package tech.standalonetc.host.struct.effector

/**
 * Position output
 *
 * Position output is an effector capable of outputting position.
 * Such as a servo or a position-close-loop motor.
 */
interface PositionOutput {

    /** Position source */
    val position: OutputDriver<Double>

}
