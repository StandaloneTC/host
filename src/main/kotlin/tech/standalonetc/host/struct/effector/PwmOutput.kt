package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.core.intefaces.ISource
import org.mechdancer.dependency.Component

/**
 * Pwm output
 *
 * Pwm output marked this device is controlled using pwm,
 * and have the ability to disable or enable its pwm.
 */
interface PwmOutput : Component {

    val pwmEnable: ISource<Boolean>

}
