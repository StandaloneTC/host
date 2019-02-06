package tech.standalonetc.host.struct.effector

import org.mechdancer.dataflow.blocks.IBroadcastBlock

/**
 * PowerOutput
 *
 * PowerOutput is an effector capable of outputting power.
 * Such as a motor or a continuous servo.
 */
interface PowerOutput {

    /**Power source*/
    val power: IBroadcastBlock<Double>

}
