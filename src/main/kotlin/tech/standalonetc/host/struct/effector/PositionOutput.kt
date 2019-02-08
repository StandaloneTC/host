package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.core.intefaces.ISource
import org.mechdancer.dependency.Component

/**
 * Position output
 *
 * Position output is an effector capable of outputting position.
 * Such as a servo or a position-close-loop motor.
 */
interface PositionOutput : Component {

    /** Position source */
    val position: ISource<Double>

}
