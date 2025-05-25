package io.openems.edge.meter.evcc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.openems.edge.bridge.http.api.HttpError;
import io.openems.edge.bridge.http.api.HttpResponse;
import io.openems.edge.bridge.http.dummy.DummyBridgeHttpBundle;
import io.openems.edge.common.test.AbstractComponentTest.TestCase;
import io.openems.edge.common.test.ComponentTest;
import io.openems.edge.meter.api.ElectricityMeter;

public class EvccLoadpointMeterTest {

	@Test
	public void test() throws Exception {
		final var sut = new EvccLoadpointMeterImpl();
		final var httpTestBundle = new DummyBridgeHttpBundle();
		new ComponentTest(sut) //
				.addReference("httpBridgeFactory", httpTestBundle.factory()) //
				.activate(MyConfig.create() //
						.setId("meter1") //
						.setIp("172.0.0.1") //
						.setPort("7070") //
						.setLoadpointNumber("0") //
						.build()) //

				.next(new TestCase("Successful read response") //
						.onBeforeProcessImage(() -> {
							httpTestBundle.forceNextSuccessfulResult(HttpResponse.ok("""
									{
									    "result": {
									        "aux": [],
									        "battery": [{
									                "power": -140,
									                "capacity": 10.2,
									                "soc": 63,
									                "controllable": true
									            }
									        ],
									        "batteryCapacity": 10.2,
									        "batteryDischargeControl": true,
									        "batteryEnergy": 0,
									        "batteryGridChargeActive": false,
									        "batteryMode": "unknown",
									        "batteryPower": -140,
									        "batterySoc": 63,
									        "bufferSoc": 80,
									        "bufferStartSoc": 0,
									        "circuits": {
									            "main": {
									                "power": 10,
									                "current": 5.78,
									                "maxCurrent": 35
									            }
									        },
									        "currency": "EUR",
									        "eebus": false,
									        "ext": [],
									        "greenShareHome": 1,
									        "greenShareLoadpoints": 0.997,
									        "grid": {
									            "power": 10,
									            "energy": 14044.923,
									            "powers": [-700, 1160, -440],
									            "currents": [-3.88, 5.8, -3.33]
									        },
									        "gridConfigured": true,
									        "hems": {},
									        "homePower": 2515.52,
									        "influx": {
									            "url": "",
									            "database": "",
									            "token": "",
									            "org": "",
									            "user": "",
									            "password": "",
									            "insecure": false
									        },
									        "interval": 10,
									        "loadpoints": [{
									                "batteryBoost": false,
									                "chargeCurrents": [6.049070835, 6.092969894, 6.005991936],
									                "chargeDuration": 9200,
									                "chargePower": 3847.48,
									                "chargeRemainingDuration": 1218,
									                "chargeRemainingEnergy": 1302.245,
									                "chargeTotalImport": 6663.93,
									                "chargeVoltages": [224.5875549, 228.2381134, 221.0879211],
									                "chargedEnergy": 7420.896,
									                "chargerFeatureHeating": false,
									                "chargerFeatureIntegratedDevice": false,
									                "chargerIcon": null,
									                "chargerPhases1p3p": true,
									                "chargerSinglePhase": false,
									                "chargerStatusReason": "unknown",
									                "charging": true,
									                "connected": true,
									                "connectedDuration": 0,
									                "disableDelay": 180,
									                "disableThreshold": 0,
									                "effectiveLimitSoc": 80,
									                "effectiveMaxCurrent": 16,
									                "effectiveMinCurrent": 6,
									                "effectivePlanId": 0,
									                "effectivePlanSoc": 0,
									                "effectivePlanTime": null,
									                "effectivePriority": 0,
									                "enableDelay": 60,
									                "enableThreshold": 0,
									                "enabled": true,
									                "limitEnergy": 0,
									                "limitSoc": 80,
									                "maxCurrent": 16,
									                "minCurrent": 6,
									                "mode": "pv",
									                "offeredCurrent": 6.087,
									                "phaseAction": "inactive",
									                "phaseRemaining": 0,
									                "phasesActive": 3,
									                "phasesConfigured": 0,
									                "planActive": false,
									                "planEnergy": 0,
									                "planOverrun": 0,
									                "planPrecondition": 0,
									                "planProjectedEnd": null,
									                "planProjectedStart": null,
									                "planTime": null,
									                "priority": 0,
									                "pvAction": "inactive",
									                "pvRemaining": 0,
									                "sessionCo2PerKWh": 0.685,
									                "sessionEnergy": 7420.896,
									                "sessionPrice": 0.695,
									                "sessionPricePerKWh": 0.094,
									                "sessionSolarPercentage": 99.633,
									                "smartCostActive": false,
									                "smartCostLimit": null,
									                "smartCostNextStart": null,
									                "title": "Garage",
									                "vehicleClimaterActive": false,
									                "vehicleDetectionActive": false,
									                "vehicleLimitSoc": 0,
									                "vehicleName": "e-208",
									                "vehicleOdometer": 45434.4,
									                "vehicleRange": 212,
									                "vehicleSoc": 77.761,
									                "vehicleWelcomeActive": false
									            }, {
									                "batteryBoost": false,
									                "chargeCurrents": [0, 0, 0],
									                "chargeDuration": 0,
									                "chargePower": 0,
									                "chargeTotalImport": 6539.884,
									                "chargeVoltages": [230.2042542, 0, 0],
									                "chargedEnergy": 0,
									                "chargerFeatureHeating": false,
									                "chargerFeatureIntegratedDevice": false,
									                "chargerIcon": null,
									                "chargerPhases1p3p": true,
									                "chargerSinglePhase": false,
									                "charging": false,
									                "connected": true,
									                "connectedDuration": 0,
									                "disableDelay": 180,
									                "disableThreshold": 0,
									                "effectiveLimitSoc": 85,
									                "effectiveMaxCurrent": 16,
									                "effectiveMinCurrent": 8,
									                "effectivePlanId": 0,
									                "effectivePlanSoc": 0,
									                "effectivePlanTime": null,
									                "effectivePriority": 0,
									                "enableDelay": 60,
									                "enableThreshold": 0,
									                "enabled": false,
									                "limitEnergy": 0,
									                "limitSoc": 85,
									                "maxCurrent": 16,
									                "minCurrent": 6,
									                "mode": "pv",
									                "offeredCurrent": 0,
									                "phaseAction": "inactive",
									                "phaseRemaining": 0,
									                "phasesActive": 3,
									                "phasesConfigured": 0,
									                "planActive": false,
									                "planEnergy": 0,
									                "planOverrun": 0,
									                "planPrecondition": 0,
									                "planProjectedEnd": null,
									                "planProjectedStart": null,
									                "planTime": null,
									                "priority": 0,
									                "pvAction": "inactive",
									                "pvRemaining": 0,
									                "sessionCo2PerKWh": null,
									                "sessionEnergy": 0,
									                "sessionPrice": null,
									                "sessionPricePerKWh": null,
									                "sessionSolarPercentage": 0,
									                "smartCostActive": false,
									                "smartCostLimit": null,
									                "smartCostNextStart": null,
									                "title": "Stellplatz",
									                "vehicleClimaterActive": null,
									                "vehicleDetectionActive": false,
									                "vehicleLimitSoc": 0,
									                "vehicleName": "zoe",
									                "vehicleOdometer": 111057,
									                "vehicleRange": 205,
									                "vehicleSoc": 85,
									                "vehicleWelcomeActive": false
									            }
									        ]
									    }
									}

																		"""));
							httpTestBundle.triggerNextCycle();
						}) //
						.onAfterProcessImage(() -> assertEquals("|3847 W", sut.debugLog()))

						.output(ElectricityMeter.ChannelId.ACTIVE_POWER, 3847) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, 1344) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, 1368) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, 1326) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L1, 224588) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L2, 228238) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L3, 221088) //
						// .output(ElectricityMeter.ChannelId.CURRENT, 0) //
						.output(ElectricityMeter.ChannelId.CURRENT_L1, 6049) //
						.output(ElectricityMeter.ChannelId.CURRENT_L2, 6093) //
						.output(ElectricityMeter.ChannelId.CURRENT_L3, 6006) //
						.output(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, null)) //

				.next(new TestCase("Invalid read response").onBeforeProcessImage(() -> {
					httpTestBundle.forceNextFailedResult(HttpError.ResponseError.notFound());
					httpTestBundle.triggerNextCycle();
				}) //
						.onAfterProcessImage(() -> assertEquals("?|UNDEFINED", sut.debugLog()))

						.output(ElectricityMeter.ChannelId.ACTIVE_POWER, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L1, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L2, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_POWER_L3, null) //
						.output(ElectricityMeter.ChannelId.VOLTAGE, null) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L1, null) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L2, null) //
						.output(ElectricityMeter.ChannelId.VOLTAGE_L3, null) //
						.output(ElectricityMeter.ChannelId.CURRENT, null) //
						.output(ElectricityMeter.ChannelId.CURRENT_L1, null) //
						.output(ElectricityMeter.ChannelId.CURRENT_L2, null) //
						.output(ElectricityMeter.ChannelId.CURRENT_L3, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_CONSUMPTION_ENERGY, null) //
						.output(ElectricityMeter.ChannelId.ACTIVE_PRODUCTION_ENERGY, null)) //

				.deactivate();
	}

}
