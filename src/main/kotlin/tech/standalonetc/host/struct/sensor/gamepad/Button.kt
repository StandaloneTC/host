package tech.standalonetc.host.struct.sensor.gamepad

import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.broadcast
import tech.standalonetc.host.struct.TreeComponent
import tech.standalonetc.host.struct.sensor.Sensor
import tech.standalonetc.host.struct.tryPost
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Button
 */
class Button(name: String, gamepad: Gamepad)
    : TreeComponent(name, gamepad),
      Sensor<Boolean> {
    private val _pressed = AtomicBoolean(false)

    /** Current button state */
    val pressed get() = _pressed.get()

    /** Pressing event */
    val pressing: ISource<Unit> = broadcast()

    /** Releasing event */
    val releasing: ISource<Unit> = broadcast()

    override val updated: ISource<Boolean> = broadcast()

    override fun update(new: Boolean) {
        if (_pressed.getAndSet(new) != new) {
            updated tryPost new
            (if (new) pressing else releasing) tryPost Unit
        }
    }
}
