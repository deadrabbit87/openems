package io.openems.edge.evse.chargepoint.bender.cc;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.edge.common.type.Phase.SingleOrThreePhase;
import io.openems.edge.evse.chargepoint.bender.cc.enums.WallboxModel;
import io.openems.edge.meter.api.PhaseRotation;

@ObjectClassDefinition(name = "EVSE Charge-Point Bender CC", //
		description = "Implements a Bender CC612/CC613 based electric vehicle charging station")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "evseChargePoint0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Wallbox Model", description = "The wallbox model using the Bender CC controller")
	WallboxModel wallboxModel() default WallboxModel.BENDER_CC613;

	@AttributeDefinition(name = "Read only", description = "Defines that this evse is read only.", required = true)
	boolean readOnly() default false;

	@AttributeDefinition(name = "Debug Mode", description = "Activates the debug mode")
	boolean debugMode() default false;

	@AttributeDefinition(name = "Hardware Wiring", description = "Single or three phase hardware wiring", required = true)
	SingleOrThreePhase wiring() default SingleOrThreePhase.THREE_PHASE;

	@AttributeDefinition(name = "Phase Rotation", description = "Apply standard or rotated wiring")
	PhaseRotation phaseRotation() default PhaseRotation.L1_L2_L3;

	@AttributeDefinition(name = "Minimum Hardware Current [mA]", description = "The minimum current supported by the hardware in milliampere")
	int minHwCurrent() default 6000;

	@AttributeDefinition(name = "Maximum Hardware Current [mA]", description = "The maximum current supported by the hardware in milliampere")
	int maxHwCurrent() default 32000;

	@AttributeDefinition(name = "Modbus-ID", description = "ID of Modbus bridge")
	String modbus_id() default "modbus0";

	@AttributeDefinition(name = "Modbus Unit-ID", description = "The Unit-ID of the Modbus device.")
	int modbusUnitId() default 255;

	String webconsole_configurationFactory_nameHint() default "EVSE Charge-Point Bender CC [{id}]";
}
