package io.openems.edge.evse.chargepoint.bender.cc;

import static io.openems.common.channel.AccessMode.WRITE_ONLY;
import static io.openems.common.channel.Unit.MILLIAMPERE;
import static io.openems.common.channel.Unit.PERCENT;
import static io.openems.common.channel.Unit.WATT_HOURS;
import static io.openems.common.types.OpenemsType.BOOLEAN;
import static io.openems.common.types.OpenemsType.INTEGER;
import static io.openems.common.types.OpenemsType.STRING;

import io.openems.common.channel.Unit;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.evse.chargepoint.bender.cc.enums.ChargePointState;
import io.openems.edge.evse.chargepoint.bender.cc.enums.OcppChargePointStatus;
import io.openems.edge.evse.chargepoint.bender.cc.enums.RelayState;

public interface EvseChargePointBenderCc extends OpenemsComponent {

	public enum ChannelId implements io.openems.edge.common.channel.ChannelId {

		/**
		 * Firmware Version (Register 100-101, uint32 MSWLSW).
		 */
		FIRMWARE_VERSION(Doc.of(OpenemsType.LONG)),

		/**
		 * OCPP Charge-Point Status (Register 104).
		 */
		OCPP_CP_STATUS(Doc.of(OcppChargePointStatus.values())),

		/**
		 * Modbus Protocol Version (Register 120).
		 */
		MODBUS_PROTOCOL_VERSION(Doc.of(INTEGER)),

		/**
		 * Charge-Point State / IEC 61851 (Register 122).
		 */
		CHARGE_POINT_STATE(Doc.of(ChargePointState.values())),

		/**
		 * Relay State (Register 140).
		 */
		RELAY_STATE(Doc.of(RelayState.values())),

		/**
		 * Charge-Point Model (Register 142-151, string 20 bytes).
		 */
		CHARGE_POINT_MODEL(Doc.of(STRING)),

		/**
		 * Energy L1 (Register 200-201, uint32, Wh).
		 */
		ENERGY_L1(Doc.of(OpenemsType.LONG) //
				.unit(WATT_HOURS)),

		/**
		 * Energy L2 (Register 202-203, uint32, Wh).
		 */
		ENERGY_L2(Doc.of(OpenemsType.LONG) //
				.unit(WATT_HOURS)),

		/**
		 * Energy L3 (Register 204-205, uint32, Wh).
		 */
		ENERGY_L3(Doc.of(OpenemsType.LONG) //
				.unit(WATT_HOURS)),

		/**
		 * User ID / RFID (Register 720-729, string 20 bytes).
		 */
		USER_ID(Doc.of(STRING)),

		/**
		 * EV Battery State (Register 730, %).
		 */
		EV_BATTERY_STATE(Doc.of(INTEGER) //
				.unit(PERCENT)),

		/**
		 * Smart Vehicle Detected (Register 740).
		 */
		SMART_VEHICLE_DETECTED(Doc.of(BOOLEAN)),

		/**
		 * EVCCID (Register 741-746, string 12 bytes).
		 */
		EVCCID(Doc.of(STRING)),

		/**
		 * Debug mirror for SET_HEMS_CURRENT.
		 */
		DEBUG_SET_HEMS_CURRENT(Doc.of(INTEGER) //
				.unit(MILLIAMPERE)),

		/**
		 * HEMS Current Limit (Register 1001, R/W, 0.1A, SCALE_FACTOR_2 → mA).
		 */
		SET_HEMS_CURRENT(Doc.of(INTEGER) //
				.unit(MILLIAMPERE) //
				.accessMode(WRITE_ONLY) //
				.onChannelSetNextWriteMirrorToDebugChannel(DEBUG_SET_HEMS_CURRENT)),

		/**
		 * Active Power L1 (Register 206-207, uint32, W). Used internally for
		 * phase-rotated mapping.
		 */
		ACTIVE_POWER_L1(Doc.of(INTEGER) //
				.unit(Unit.WATT)),

		/**
		 * Active Power L2 (Register 208-209, uint32, W). Used internally for
		 * phase-rotated mapping.
		 */
		ACTIVE_POWER_L2(Doc.of(INTEGER) //
				.unit(Unit.WATT)),

		/**
		 * Active Power L3 (Register 210-211, uint32, W). Used internally for
		 * phase-rotated mapping.
		 */
		ACTIVE_POWER_L3(Doc.of(INTEGER) //
				.unit(Unit.WATT)),
		;

		private final Doc doc;

		private ChannelId(Doc doc) {
			this.doc = doc;
		}

		@Override
		public Doc doc() {
			return this.doc;
		}
	}

	/**
	 * Gets the Channel for {@link ChannelId#SET_HEMS_CURRENT}.
	 *
	 * @return the Channel
	 */
	public default IntegerWriteChannel getSetHemsCurrentChannel() {
		return this.channel(ChannelId.SET_HEMS_CURRENT);
	}

	/**
	 * Sets the write value of the {@link ChannelId#SET_HEMS_CURRENT} Channel used
	 * to set the HEMS current limit in [mA].
	 *
	 * @param value the next value in milliampere
	 * @throws OpenemsNamedException on error
	 */
	public default void setHemsCurrent(Integer value) throws OpenemsNamedException {
		this.getSetHemsCurrentChannel().setNextWriteValue(value);
	}
}
