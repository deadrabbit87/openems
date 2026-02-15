package io.openems.edge.evse.chargepoint.bender.cc;

import static io.openems.edge.common.type.Phase.SingleOrThreePhase.THREE_PHASE;
import static io.openems.edge.evse.chargepoint.bender.cc.enums.WallboxModel.BENDER_CC613;
import static io.openems.edge.meter.api.PhaseRotation.L1_L2_L3;

import org.junit.Test;

import io.openems.common.test.DummyConfigurationAdmin;
import io.openems.edge.bridge.modbus.test.DummyModbusBridge;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;

public class EvseChargePointBenderCcImplTest {

	@Test
	public void test() throws Exception {
		new ComponentTest(new EvseChargePointBenderCcImpl()) //
				.addReference("cm", new DummyConfigurationAdmin()) //
				.addReference("setModbus", new DummyModbusBridge("modbus0")) //
				.activate(MyConfig.create() //
						.setId("evseChargePoint0") //
						.setModbusId("modbus0") //
						.setModbusUnitId(255) //
						.setWallboxModel(BENDER_CC613) //
						.setWiring(THREE_PHASE) //
						.setPhaseRotation(L1_L2_L3) //
						.setReadOnly(false) //
						.setDebugMode(false) //
						.setMinHwCurrent(6000) //
						.setMaxHwCurrent(32000) //
						.build()) //
				.next(new TestCase()) //
				.deactivate();
	}
}
