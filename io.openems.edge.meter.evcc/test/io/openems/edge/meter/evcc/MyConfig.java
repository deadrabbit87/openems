package io.openems.edge.meter.evcc;

import io.openems.common.test.AbstractComponentConfig;
import io.openems.common.types.MeterType;

@SuppressWarnings("all")
public class MyConfig extends AbstractComponentConfig implements Config {

	protected static class Builder {
		private String id;
		private String ip;
		private String port;
		private String loadpointNumber;
		private MeterType meterType; 

		private Builder() {
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public Builder setIp(String ip) {
			this.ip = ip;
			return this;
		}

		public Builder setPort(String port) {
			this.port = port;
			return this;
		}

		public Builder setLoadpointNumber(String loadpointNumber) {
			this.loadpointNumber = loadpointNumber;
			return this;
		}
		
		public Builder setMeterType(String meterType) {
			this.setMeterType(meterType); 
			return this; 
		}

		public MyConfig build() {
			return new MyConfig(this);
		}
	}

	/**
	 * Create a Config builder.
	 * 
	 * @return a {@link Builder}
	 */
	public static Builder create() {
		return new Builder();
	}

	private final Builder builder;

	private MyConfig(Builder builder) {
		super(Config.class, builder.id);
		this.builder = builder;
	}

	@Override
	public String port() {
		return this.builder.port;
	}

	@Override
	public String ip() {
		return this.builder.ip;
	}

	@Override
	public String loadpointNumber() {
		return this.builder.loadpointNumber;
	}
	
	@Override
	public MeterType type() {
		return this.builder.meterType;
	}

}