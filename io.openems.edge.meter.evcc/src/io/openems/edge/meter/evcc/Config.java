package io.openems.edge.meter.evcc;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "io.openems.edge.meter.evcc", //
		description = "Displays a evcc loadpoint as consumption meter")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "io.openems.edge.meter.evcc0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "IP-Address", description = "The IP address of the evcc instance.")
	String ip();
	
	@AttributeDefinition(name = "Port", description = "The port of the evcc instance.")
	String port() default "7070";

	String webconsole_configurationFactory_nameHint() default "io.openems.edge.meter.evcc [{id}]";

}