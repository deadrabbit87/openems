package io.openems.edge.meter.evcc;

import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static io.openems.common.utils.JsonUtils.getAsFloat;
import static io.openems.common.utils.JsonUtils.getAsInt;
import static io.openems.common.utils.JsonUtils.getAsJsonArray; 
import static java.lang.Math.round;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.osgi.service.event.propertytypes.EventTopics;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.MeterType;
import io.openems.edge.bridge.http.api.BridgeHttp;
import io.openems.edge.bridge.http.api.BridgeHttpFactory;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.meter.evcc", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class EvccLoadpointMeterImpl extends AbstractOpenemsComponent implements EvccLoadpointMeter, OpenemsComponent, TimedataProvider, EventHandler, ElectricityMeter {

	private final CalculateEnergyFromPower calculateProductionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY);
	private final CalculateEnergyFromPower calculateConsumptionEnergy = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY);
	
	private Config config = null;
	private final Logger log = LoggerFactory.getLogger(EvccLoadpointMeterImpl.class);

	private MeterType meterType = MeterType.CONSUMPTION_METERED;
	private String baseUrl;
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata;

	@Reference()
	private BridgeHttpFactory httpBridgeFactory;
	private BridgeHttp httpBridge;

	public EvccLoadpointMeterImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				EvccLoadpointMeter.ChannelId.values() //
		);
	}

	@Activate
	private void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
		this.baseUrl = "http://" + config.ip() + ":" + config.port();
		
		this.httpBridge = this.httpBridgeFactory.get();
		
		if (this.isEnabled()) {
			this.httpBridge.subscribeJsonEveryCycle(this.baseUrl + "/api/state", this::processHttpResult);
		}

	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}
	
	@Override
	public String debugLog() {
		var b = new StringBuilder();
		b.append("|").append(getActivePowerChannel().value().asString());
		return b.toString();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}

		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE //
			-> this.calculateEnergy();
		}
	}
	
	private void processHttpResult(HttpResponse<JsonElement> result, Throwable error) {
		this._setSlaveCommunicationFailed(result == null);
		
		// Prepare variables
		Integer activePower = null; 
		Integer currentL1 = null;
		Integer currentL2 = null;
		Integer currentL3 = null;
		Integer loadpointNumber = Integer.parseInt(config.loadpointNumber()); 
		
		if (error != null) {
			this.logDebug(this.log, error.getMessage());
		} else {
			try {
				var response = getAsJsonObject(result.data()); 
	            var resultObject = response.getAsJsonObject("result");
				activePower = round(getAsFloat(resultObject, "homePower"));
				
				var loadpoints = getAsJsonArray(resultObject, "loadpoints");
				for (int i = 0; i < loadpoints.size(); i++) {
					var loadpoint = loadpoints.get(i); 
					var chargePower = round(getAsFloat(loadpoint, "chargePower")); 
					System.out.println("charge power loadpoint " + i + " : " + chargePower + " W"); 
					
				    var chargeCurrents = getAsJsonArray(loadpoint, "chargeCurrents"); 
				    var chargeVoltages = getAsJsonArray(loadpoint, "chargeVoltages"); 
				    
				    for (int j = 0; j < chargeCurrents.size(); j++) {
				        var current = round(getAsFloat(chargeCurrents.get(j)) * 1000); 
				        var voltage = round(getAsFloat(chargeVoltages.get(j))); 
				        
				        System.out.println("L" + (j + 1) + ": " + current); 
				        System.out.println("L" + (j + 1) + ": " + voltage); 
				        
						// Actually set Channels
				        if(loadpointNumber == j) {
					        switch (j + 1) {
				            case 1:
				                this._setCurrentL1(current);
				                this._setVoltageL1(voltage); 
				                break;
				            case 2:
				                this._setCurrentL2(current);
				                this._setVoltageL2(voltage);
				                break;
				            case 3:
				                this._setCurrentL3(current);
				                this._setVoltageL3(voltage); 
				                break;
					        }
				        }
				    }
					}
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}
		
		// Actually set Channels
		this._setActivePower(activePower);
	}
	
	/**
	 * Calculate the Energy values from ActivePower.
	 */
	private void calculateEnergy() {
		// Calculate Energy
		final var activePower = this.getActivePower().get();
		if (activePower == null) {
			this.calculateProductionEnergy.update(null);
			this.calculateConsumptionEnergy.update(null);
		} else if (activePower >= 0) {
			this.calculateProductionEnergy.update(activePower);
			this.calculateConsumptionEnergy.update(0);
		} else {
			this.calculateProductionEnergy.update(0);
			this.calculateConsumptionEnergy.update(-activePower);
		}
	}

	@Override
	public MeterType getMeterType() {
		return this.meterType; 
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}
}
