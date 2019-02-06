package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.blocks.IBroadcastBlock

/**
 * Position output
 *
 * Position output is an effector capable of outputting position.
 * Such as a servo or a position-close-loop motor.
 */
interface PositionOutput {

    /** Position source */
    val position: IBroadcastBlock<Double>

}
