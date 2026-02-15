package io.openems.edge.evse.chargepoint.bender.cc.enums;

import io.openems.common.types.OptionsEnum;

/**
 * OCPP Charge-Point Status from Bender CC Register 104.
 */
public enum OcppChargePointStatus implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	AVAILABLE(0, "Available"), //
	OCCUPIED(1, "Occupied"), //
	RESERVED(2, "Reserved"), //
	UNAVAILABLE(3, "Unavailable"), //
	FAULTED(4, "Faulted"), //
	PREPARING(5, "Preparing"), //
	CHARGING(6, "Charging"), //
	SUSPENDED_EVSE(7, "SuspendedEVSE"), //
	SUSPENDED_EV(8, "SuspendedEV"), //
	FINISHING(9, "Finishing"), //
	;

	private final int value;
	private final String name;

	private OcppChargePointStatus(int value, String name) {
		this.value = value;
		this.name = name;
	}

	@Override
	public int getValue() {
		return this.value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public OptionsEnum getUndefined() {
		return UNDEFINED;
	}
}
