package tech.standalonetc.host.struct.effector

/**
 * Pwm output
 *
 * Pwm output marked this device is controlled using pwm,
 * and have the ability to disable or enable its pwm.
 */
interface PwmOutput {

    val pwmEnable: OutputDriver<Boolean>

}
