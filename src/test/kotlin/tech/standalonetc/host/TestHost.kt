package tech.standalonetc.host

import org.junit.Before
import org.junit.Test
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dependency.plusAssign
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.preset.chassis.MecanumChassis
import tech.standalonetc.host.struct.sensor.gamepad.Gamepad

class TestHost {

    private lateinit var robot: Robot

    private lateinit var gamepad: Gamepad

    private val chassis = MecanumChassis()

    @Before
    fun before() {
        robot = Robot(loggingNetwork = true, loggingRemoteHub = true)

        robot += chassis

        gamepad = robot.master


        val devices= deviceBundle {
            "chassis" {
                motor("LF", Motor.Direction.REVERSED)
                motor("LB", Motor.Direction.REVERSED)
                motor("RF", Motor.Direction.FORWARD)
                motor("RB", Motor.Direction.FORWARD)
            }
        }

        robot.setupDeviceBundle(devices)

        robot.init(*devices.idMapping.toList().toTypedArray())

    }

    @Test
    fun test() {
        gamepad.updated - {
            MecanumChassis.Descartes(
                it.leftStickY,
                it.leftStickX,
                -it.rightStickX
            )
        } - chassis.descartesControl
    }
}
