package io.openems.edge.meter.evcc;

import static io.openems.common.utils.JsonUtils.getAsJsonObject;
import static io.openems.common.utils.JsonUtils.getAsFloat;
import static io.openems.common.utils.JsonUtils.getAsJsonArray; 
import static java.lang.Math.round;


import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.meter.api.ElectricityMeter;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "io.openems.edge.meter.evcc", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class EvccLoadpointMeterImpl extends AbstractOpenemsComponent implements EvccLoadpointMeter, OpenemsComponent, EventHandler, ElectricityMeter {

	private Config config = null;
	private final Logger log = LoggerFactory.getLogger(EvccLoadpointMeterImpl.class);

	private MeterType meterType = MeterType.CONSUMPTION_METERED;
	private String baseUrl;

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
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE:
			// TODO: fill channels
			break;
		}
	}
	
	private void processHttpResult(HttpResponse<JsonElement> result, Throwable error) {
		this._setSlaveCommunicationFailed(result == null);
		
		// Prepare variables
		Integer activePower = null; 
		
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
					System.out.println(chargePower); 
				}
			} catch (OpenemsNamedException e) {
				this.logDebug(this.log, e.getMessage());
			}
		}
		
		this._setActivePower(activePower);
	}

	@Override
	public String debugLog() {
		return "Hello World";
	}

	@Override
	public MeterType getMeterType() {
		return this.meterType; 
	}
}
