package tech.standalonetc.host

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dependency.*
import tech.standalonetc.host.struct.RobotComponent
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.effector.PowerOutput

object KotoriRobot : Robot() {
    init {
        setIdMapping(*KotoriDeviceBundle.idMaps)
        setupDeviceBundle(KotoriDeviceBundle)
    }
}

object KotoriDeviceBundle : DeviceBundle() {
    init {
        motor("apple")
    }
}

object AppleArm : UniqueComponent<AppleArm>(), RobotComponent, Dependent, PowerOutput {
    private val manager = DependencyManager()
    override fun sync(dependency: Component): Boolean = manager.sync(dependency)

    private val motor by manager.must<Motor>("apple")

    override val power: DataBlock<Double> = broadcast()

    override fun init() {
        power - { it - 0.1/*某些操作*/ } - motor.power
    }
}

class KotoriTeleOp : RobotProgram<KotoriRobot>() {
    override fun onOpModeInit() {
        robot.master.leftStick.valueChanged - { it.x } - AppleArm.power
    }

}

fun main() {
    val runner = RobotProgramRunner<KotoriRobot>()
    runner.switchProgram(KotoriTeleOp::class)
}