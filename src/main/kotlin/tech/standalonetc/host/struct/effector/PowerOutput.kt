package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.core.intefaces.ISource
import org.mechdancer.dependency.Component

/**
 * PowerOutput
 *
 * PowerOutput is an effector capable of outputting power.
 * Such as a motor or a continuous servo.
 */
interface PowerOutput : Component {

    /**Power source*/
    val power: ISource<Double>

}
