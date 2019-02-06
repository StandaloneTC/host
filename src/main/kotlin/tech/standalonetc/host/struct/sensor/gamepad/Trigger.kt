package tech.standalonetc.host.struct.sensor.gamepad

import org.mechdancer.dataflow.blocks.BroadcastBlock
import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.post
import tech.standalonetc.host.struct.TreeComponent
import tech.standalonetc.host.struct.sensor.Sensor
import java.util.concurrent.atomic.AtomicReference

/**
 * Trigger
 */
class Trigger(name: String, parent: Gamepad) : TreeComponent(name, parent),
                                               Sensor<Double> {
    private val _value = AtomicReference(.0)

    /** This trigger is counted as pressing when its [value] > [pressingThreshold] */
    @Volatile
    var pressingThreshold = 0.7

    /** Current value */
    val value: Double get() = _value.get()

    /** Current state */
    val pressed get() = value > pressingThreshold

    /** Pressing event */
    val pressing: ISource<Unit> = broadcast()

    /** Releasing event */
    val releasing: ISource<Unit> = broadcast()

    override val updated: ISource<Double> = broadcast()

    override fun update(new: Double) {
        val last = _value.getAndSet(new)
        if (last != new) {
            (updated as BroadcastBlock<Double>) post new
            when (pressingThreshold) {
                in last..new -> (pressing as BroadcastBlock<Unit>) post Unit
                in new..last -> (releasing as BroadcastBlock<Unit>) post Unit
            }
        }
    }
}
