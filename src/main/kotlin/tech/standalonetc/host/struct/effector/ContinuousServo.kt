package tech.standalonetc.host.struct.effector

import org.mechdancer.dependency.NamedComponent
import tech.standalonetc.host.checkedValue
import tech.standalonetc.host.logger
import tech.standalonetc.host.struct.Device

/**
 * ContinuousServo
 *
 * A kind of device realized power output using pwm.
 */
class ContinuousServo(name: String) :
    NamedComponent<ContinuousServo>(name), Device, PowerOutput, PwmOutput {

    override val power: OutputDriver<Double> = OutputDriver {
        it.checkedValue(-1.0..1.0)
            ?: logger.warning("Invalid continuous servo power: $it").run { null }
    }

    override val pwmEnable: OutputDriver<Boolean> = OutputDriver()

    override fun toString(): String = "${javaClass.simpleName}[$name]"

    override fun stop() {
        power.close()
        pwmEnable.close()
    }
}
