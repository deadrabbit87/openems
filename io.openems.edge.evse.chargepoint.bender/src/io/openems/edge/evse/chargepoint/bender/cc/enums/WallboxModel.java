package io.openems.edge.evse.chargepoint.bender.cc.enums;

import io.openems.common.types.OptionsEnum;

public enum WallboxModel implements OptionsEnum {
	UNDEFINED(-1, "Undefined"), //
	BENDER_CC612(0, "Bender CC612"), //
	BENDER_CC613(1, "Bender CC613"), //
	MENNEKES_AMTRON_PROFESSIONAL(2, "Mennekes AMTRON Professional"), //
	MENNEKES_AMEDIO_PROFESSIONAL(3, "Mennekes AMEDIO Professional"), //
	MENNEKES_AMTRON_CHARGE_CONTROL(4, "Mennekes AMTRON Charge Control"), //
	WEBASTO_LIVE(5, "Webasto Live"), //
	JUICE_CHARGER_ME(6, "Juice Charger me"), //
	TECHNISAT_TECHNIVOLT(7, "TechniSat TechniVolt"), //
	EBEE_WALLBOX(8, "eBee Wallbox"), //
	OPTEC_MOBILITY_ONE(9, "Optec Mobility.One"), //
	GARO_GLB(10, "GARO GLB"), //
	GARO_GLB_PLUS(11, "GARO GLB+"), //
	GARO_LS4(12, "GARO LS4"), //
	GARO_LS4_COMPACT(13, "GARO LS4 Compact"), //
	ENSTO_CHAGO(14, "Ensto Chago"), //
	UBITRICITY_HEINZ(15, "ubitricity Heinz"), //
	CUBOS_C11E(16, "CUBOS C11E"), //
	CUBOS_C22E(17, "CUBOS C22E"), //
	SPELSBERG_SMART_PRO(18, "Spelsberg Smart PRO"), //
	SMA_EV_CHARGER_BUSINESS(19, "SMA EV Charger Business"), //
	;

	private final int value;
	private final String name;

	private WallboxModel(int value, String name) {
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
