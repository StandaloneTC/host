package tech.standalonetc.host

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dependency.*
import tech.standalonetc.host.struct.RobotComponent
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.effector.PowerOutput

object ToriRobot : Robot() {
    init {
        setIdMapping(*ToriDeviceBundle.idMaps)
        setupDeviceBundle(ToriDeviceBundle)
    }
}

object ToriDeviceBundle : DeviceBundle() {
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

class ToriTeleOp : RobotProgram<ToriRobot>() {
    override fun run() {
        robot.master.leftStick.valueChanged - { it.x } - AppleArm.power
    }

    override fun close() {
    }
}

fun main() {
    val runner = RobotProgramRunner<ToriRobot>()
    runner.switchProgram(ToriTeleOp::class)
}