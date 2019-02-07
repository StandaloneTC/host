package tech.standalonetc.host.struct.effector

/**
 * PowerOutput
 *
 * PowerOutput is an effector capable of outputting power.
 * Such as a motor or a continuous servo.
 */
interface PowerOutput {

    /**Power source*/
    val power: OutputDriver<Double>

}
