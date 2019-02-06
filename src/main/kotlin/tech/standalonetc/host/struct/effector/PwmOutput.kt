package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.blocks.IBroadcastBlock

/**
 * Pwm output
 *
 * Pwm output marked this device is controlled using pwm,
 * and have the ability to disable or enable its pwm.
 */
interface PwmOutput {

    val pwmEnable: IBroadcastBlock<Boolean>

}
