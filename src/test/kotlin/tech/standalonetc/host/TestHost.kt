package tech.standalonetc.host

import org.junit.Before
import org.junit.Test
import org.mechdancer.dataflow.core.minus
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.preset.chassis.MecanumChassis
import tech.standalonetc.host.struct.sensor.gamepad.Gamepad
import tech.standalonetc.host.struct.setupRecursive

class TestHost {

    private lateinit var robot: Robot

    private lateinit var gamepad: Gamepad

    private val chassis = MecanumChassis()

    @Before
    fun before() {
        robot = Robot()

        robot.setupRecursive(chassis)

        gamepad = robot.master

        robot.setupDeviceBundleAndInit {
            "chassis" {
                motor("LF", Motor.Direction.REVERSED)
                motor("LB", Motor.Direction.REVERSED)
                motor("RF", Motor.Direction.FORWARD)
                motor("RB", Motor.Direction.FORWARD)
            }
        }

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
