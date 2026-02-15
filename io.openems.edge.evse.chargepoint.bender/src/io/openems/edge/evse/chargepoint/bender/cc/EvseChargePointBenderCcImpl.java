package io.openems.edge.evse.chargepoint.bender.cc;

import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_2;
import static io.openems.edge.bridge.modbus.api.ElementToChannelConverter.SCALE_FACTOR_3;
import static io.openems.edge.common.channel.ChannelUtils.setValue;
import static io.openems.edge.common.type.Phase.SingleOrThreePhase.SINGLE_PHASE;
import static io.openems.edge.common.type.Phase.SingleOrThreePhase.THREE_PHASE;
import static io.openems.edge.common.type.Phase.SinglePhase.L1;
import static io.openems.edge.common.type.Phase.SinglePhase.L2;
import static io.openems.edge.common.type.Phase.SinglePhase.L3;
import static io.openems.edge.meter.api.PhaseRotation.mapLongToPhaseRotatedActivePowerChannel;

import java.time.Duration;
import java.time.Instant;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
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

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.exceptions.OpenemsException;
import io.openems.common.types.Tuple;
import io.openems.edge.bridge.modbus.api.AbstractOpenemsModbusComponent;
import io.openems.edge.bridge.modbus.api.BridgeModbus;
import io.openems.edge.bridge.modbus.api.ModbusComponent;
import io.openems.edge.bridge.modbus.api.ModbusProtocol;
import io.openems.edge.bridge.modbus.api.element.DummyRegisterElement;
import io.openems.edge.bridge.modbus.api.element.StringWordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedDoublewordElement;
import io.openems.edge.bridge.modbus.api.element.UnsignedWordElement;
import io.openems.edge.bridge.modbus.api.element.WordOrder;
import io.openems.edge.bridge.modbus.api.task.FC3ReadRegistersTask;
import io.openems.edge.bridge.modbus.api.task.FC6WriteRegisterTask;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.common.taskmanager.Priority;
import io.openems.edge.common.type.Phase.SingleOrThreePhase;
import io.openems.edge.evse.api.chargepoint.EvseChargePoint;
import io.openems.edge.evse.api.chargepoint.Profile.ChargePointAbilities;
import io.openems.edge.evse.api.chargepoint.Profile.ChargePointActions;
import io.openems.edge.evse.api.common.ApplySetPoint;
import io.openems.edge.evse.chargepoint.bender.cc.enums.ChargePointState;
import io.openems.edge.evse.chargepoint.bender.cc.enums.RelayState;
import io.openems.edge.meter.api.ElectricityMeter;
import io.openems.edge.meter.api.PhaseRotation;
import io.openems.edge.timedata.api.Timedata;
import io.openems.edge.timedata.api.TimedataProvider;
import io.openems.edge.timedata.api.utils.CalculateEnergyFromPower;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Evse.ChargePoint.Bender.CC", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE //
)
@EventTopics({ //
		EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE, //
})
public class EvseChargePointBenderCcImpl extends AbstractOpenemsModbusComponent
		implements EvseChargePointBenderCc, EvseChargePoint, ElectricityMeter, ModbusComponent, OpenemsComponent,
		TimedataProvider, EventHandler {

	private final Logger log = LoggerFactory.getLogger(EvseChargePointBenderCcImpl.class);
	private final CalculateEnergyFromPower calculateEnergyL1 = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L1);
	private final CalculateEnergyFromPower calculateEnergyL2 = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L2);
	private final CalculateEnergyFromPower calculateEnergyL3 = new CalculateEnergyFromPower(this,
			ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY_L3);

	private Config config;

	@Reference
	private ConfigurationAdmin cm;

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata = null;

	@Reference(policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MANDATORY)
	protected void setModbus(BridgeModbus modbus) {
		super.setModbus(modbus);
	}

	public EvseChargePointBenderCcImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				ModbusComponent.ChannelId.values(), //
				ElectricityMeter.ChannelId.values(), //
				EvseChargePoint.ChannelId.values(), //
				EvseChargePointBenderCc.ChannelId.values() //
		);
		ElectricityMeter.calculateSumCurrentFromPhases(this);
		ElectricityMeter.calculateAverageVoltageFromPhases(this);
		ElectricityMeter.calculateSumActivePowerFromPhases(this);
	}

	@Activate
	private void activate(ComponentContext context, Config config) throws OpenemsException {
		super.activate(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm, "Modbus",
				config.modbus_id());
		this.applyConfig(config);
	}

	@Modified
	private void modified(ComponentContext context, Config config) throws OpenemsNamedException {
		if (super.modified(context, config.id(), config.alias(), config.enabled(), config.modbusUnitId(), this.cm,
				"Modbus", config.modbus_id())) {
			return;
		}
		this.applyConfig(config);
	}

	private void applyConfig(Config config) {
		this.config = config;
	}

	@Override
	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	protected ModbusProtocol defineModbusProtocol() {
		final var phaseRotated = this.getPhaseRotation();

		return new ModbusProtocol(this,

				// Task 1: Firmware & OCPP Status (LOW)
				new FC3ReadRegistersTask(100, Priority.LOW,
						m(EvseChargePointBenderCc.ChannelId.FIRMWARE_VERSION,
								new UnsignedDoublewordElement(100).wordOrder(WordOrder.MSWLSW)),
						new DummyRegisterElement(102, 103),
						m(EvseChargePointBenderCc.ChannelId.OCPP_CP_STATUS, new UnsignedWordElement(104))),

				// Task 2: Protocol Version & CP State (HIGH)
				new FC3ReadRegistersTask(120, Priority.HIGH,
						m(EvseChargePointBenderCc.ChannelId.MODBUS_PROTOCOL_VERSION, new UnsignedWordElement(120)),
						new DummyRegisterElement(121),
						m(EvseChargePointBenderCc.ChannelId.CHARGE_POINT_STATE, new UnsignedWordElement(122))),

				// Task 3: Relay State & Model (LOW)
				new FC3ReadRegistersTask(140, Priority.LOW,
						m(EvseChargePointBenderCc.ChannelId.RELAY_STATE, new UnsignedWordElement(140)),
						new DummyRegisterElement(141),
						m(EvseChargePointBenderCc.ChannelId.CHARGE_POINT_MODEL, new StringWordElement(142, 10))),

				// Task 4: Metering - Energy, Currents, Power, Voltages (HIGH)
				new FC3ReadRegistersTask(200, Priority.HIGH,
						m(EvseChargePointBenderCc.ChannelId.ENERGY_L1, new UnsignedDoublewordElement(200)),
						m(EvseChargePointBenderCc.ChannelId.ENERGY_L2, new UnsignedDoublewordElement(202)),
						m(EvseChargePointBenderCc.ChannelId.ENERGY_L3, new UnsignedDoublewordElement(204)),
						m(new UnsignedDoublewordElement(206)).build()
								.onUpdateCallback(mapLongToPhaseRotatedActivePowerChannel(this, L1)),
						m(new UnsignedDoublewordElement(208)).build()
								.onUpdateCallback(mapLongToPhaseRotatedActivePowerChannel(this, L2)),
						m(new UnsignedDoublewordElement(210)).build()
								.onUpdateCallback(mapLongToPhaseRotatedActivePowerChannel(this, L3)),
						m(phaseRotated.channelCurrentL1(), new UnsignedDoublewordElement(212)),
						m(phaseRotated.channelCurrentL2(), new UnsignedDoublewordElement(214)),
						m(phaseRotated.channelCurrentL3(), new UnsignedDoublewordElement(216)),
						m(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, new UnsignedDoublewordElement(218)),
						m(ElectricityMeter.ChannelId.ACTIVE_POWER, new UnsignedDoublewordElement(220)),
						m(phaseRotated.channelVoltageL1(), new UnsignedWordElement(222), SCALE_FACTOR_3),
						m(phaseRotated.channelVoltageL2(), new UnsignedWordElement(223), SCALE_FACTOR_3),
						m(phaseRotated.channelVoltageL3(), new UnsignedWordElement(224), SCALE_FACTOR_3)),

				// Task 5: RFID & EV Battery (LOW)
				new FC3ReadRegistersTask(720, Priority.LOW,
						m(EvseChargePointBenderCc.ChannelId.USER_ID, new StringWordElement(720, 10)),
						m(EvseChargePointBenderCc.ChannelId.EV_BATTERY_STATE, new UnsignedWordElement(730))),

				// Task 6: Smart Vehicle & EVCCID (LOW)
				new FC3ReadRegistersTask(740, Priority.LOW,
						m(EvseChargePointBenderCc.ChannelId.SMART_VEHICLE_DETECTED, new UnsignedWordElement(740)),
						m(EvseChargePointBenderCc.ChannelId.EVCCID, new StringWordElement(741, 6))),

				// Task 7: HEMS Current Limit (Read + Write)
				new FC3ReadRegistersTask(1001, Priority.LOW,
						m(EvseChargePointBenderCc.ChannelId.SET_HEMS_CURRENT, new UnsignedWordElement(1001),
								SCALE_FACTOR_2)),
				new FC6WriteRegisterTask(1001,
						m(EvseChargePointBenderCc.ChannelId.SET_HEMS_CURRENT, new UnsignedWordElement(1001),
								SCALE_FACTOR_2))
		);
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE -> {
			this.calculateEnergyL1.update(this.getActivePowerL1Channel().getNextValue().get());
			this.calculateEnergyL2.update(this.getActivePowerL2Channel().getNextValue().get());
			this.calculateEnergyL3.update(this.getActivePowerL3Channel().getNextValue().get());

			setValue(this, EvseChargePoint.ChannelId.IS_READY_FOR_CHARGING,
					evaluateIsReadyForCharging(
							this.channel(EvseChargePointBenderCc.ChannelId.CHARGE_POINT_STATE).getNextValue()
									.asEnum()));
		}
		}
	}

	/**
	 * Evaluates if the Charge-Point is ready for charging based on the
	 * {@link ChargePointState}.
	 *
	 * @param cpState the {@link ChargePointState}
	 * @return true if ready for charging
	 */
	protected static boolean evaluateIsReadyForCharging(ChargePointState cpState) {
		return switch (cpState) {
		case STATE_B, STATE_C, STATE_D -> true;
		default -> false;
		};
	}

	@Override
	public ChargePointAbilities getChargePointAbilities() {
		var config = this.config;
		if (config == null || config.readOnly()) {
			return null;
		}

		final var phases = this.getCurrentPhases();

		final var cpState = (ChargePointState) this.channel(EvseChargePointBenderCc.ChannelId.CHARGE_POINT_STATE)
				.getNextValue().asEnum();
		final var isEvConnected = switch (cpState) {
		case STATE_B, STATE_C, STATE_D -> true;
		default -> false;
		};
		final var isReadyForCharging = switch (cpState) {
		case STATE_B, STATE_C, STATE_D -> true;
		default -> false;
		};

		return ChargePointAbilities.create() //
				.setApplySetPoint(
						new ApplySetPoint.Ability.MilliAmpere(phases, config.minHwCurrent(), config.maxHwCurrent())) //
				.setIsEvConnected(isEvConnected) //
				.setIsReadyForCharging(isReadyForCharging) //
				.build();
	}

	private SingleOrThreePhase getCurrentPhases() {
		if (this.config.wiring() == SINGLE_PHASE) {
			return SINGLE_PHASE;
		}

		final var relayState = (RelayState) this.channel(EvseChargePointBenderCc.ChannelId.RELAY_STATE).getNextValue()
				.asEnum();
		return switch (relayState) {
		case ONE_PHASE_ACTIVE -> SINGLE_PHASE;
		default -> THREE_PHASE;
		};
	}

	private Tuple<Instant, Integer> previousCurrent = null;

	@Override
	public void apply(ChargePointActions actions) {
		final var now = Instant.now();
		final var current = actions.getApplySetPointInMilliAmpere().value();

		this.handleApplyCharge(now, current);
	}

	private void handleApplyCharge(Instant now, int current) {
		if (this.previousCurrent != null && Duration.between(this.previousCurrent.a(), now).getSeconds() < 5) {
			return;
		}
		this.previousCurrent = Tuple.of(now, current);

		try {
			this.setHemsCurrent(current);
		} catch (OpenemsNamedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String debugLog() {
		var b = new StringBuilder() //
				.append("L:").append(this.getActivePower().asString());
		if (!this.config.readOnly()) {
			b //
					.append("|SetCurrent:") //
					.append(this.channel(EvseChargePointBenderCc.ChannelId.DEBUG_SET_HEMS_CURRENT).value().asString());
		}
		return b.toString();
	}

	@Override
	public PhaseRotation getPhaseRotation() {
		return this.config.phaseRotation();
	}

	@Override
	public Timedata getTimedata() {
		return this.timedata;
	}

	@Override
	public boolean isReadOnly() {
		return this.config.readOnly();
	}
}
