package io.openems.edge.evse.chargepoint.bender.cc.enums;

import io.openems.common.types.OptionsEnum;

/**
 * IEC 61851 Charge-Point States from Bender CC Register 122.
 */
public enum ChargePointState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	STATE_A(1, "No vehicle"), //
	STATE_B(2, "Connected, not ready"), //
	STATE_C(3, "Charging"), //
	STATE_D(4, "Charging, ventilation"), //
	STATE_E(5, "Error"), //
	STATE_F(6, "Not available"), //
	;

	private final int value;
	private final String name;

	private ChargePointState(int value, String name) {
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
