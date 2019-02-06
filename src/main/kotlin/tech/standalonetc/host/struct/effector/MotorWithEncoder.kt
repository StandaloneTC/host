package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.core.minus
import org.mechdancer.dependency.*
import tech.standalonetc.host.struct.RobotComponent
import tech.standalonetc.host.struct.sensor.Encoder

/**
 * Motor with encoder
 *
 * A combination of a motor and a encoder,
 * support position close loop control.
 */
class MotorWithEncoder(
    name: String,
    val controller: (Double) -> Double
) : NamedComponent<MotorWithEncoder>(name),
    RobotComponent,
    Dependent,
    PositionOutput {

    private val manager = DependencyManager()

    private val motor by manager.must<Motor>(name)
    private val encoder by manager.must<Encoder>(name)

    override val position: OutputDriver<Double> = OutputDriver()

    override fun sync(dependency: Component): Boolean = manager.sync(dependency)

    override fun init() {
        position - { controller(it - encoder.position) } - motor.power
    }

    override fun toString(): String = "${javaClass.simpleName}[$name]"

}
