package io.openems.edge.evse.chargepoint.bender.cc.enums;

import io.openems.common.types.OptionsEnum;

/**
 * Relay/Phase State from Bender CC Register 140.
 */
public enum RelayState implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	OFF(0, "Off"), //
	THREE_PHASES_ACTIVE(1, "Three phases active"), //
	ONE_PHASE_ACTIVE(5, "One phase active"), //
	;

	private final int value;
	private final String name;

	private RelayState(int value, String name) {
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
