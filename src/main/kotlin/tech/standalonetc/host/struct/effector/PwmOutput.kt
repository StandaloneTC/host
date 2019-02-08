package tech.standalonetc.host.struct.effector

import org.mechdancer.dependency.Component
import tech.standalonetc.host.DataBlock

/**
 * Pwm output
 *
 * Pwm output marked this device is controlled using pwm,
 * and have the ability to disable or enable its pwm.
 */
interface PwmOutput : Component {

    /** Pwm enable block */
    val pwmEnable: DataBlock<Boolean>

}
