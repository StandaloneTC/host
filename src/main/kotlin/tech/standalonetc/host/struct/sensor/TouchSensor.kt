package tech.standalonetc.host.struct.sensor

import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dependency.NamedComponent
import tech.standalonetc.host.struct.tryPost
import java.util.concurrent.atomic.AtomicBoolean

class TouchSensor(name: String) : NamedComponent<TouchSensor>(name), Sensor<Boolean> {
    private val _pressed = AtomicBoolean(false)

    /** Current sensor state */
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

    override fun toString(): String = "${javaClass.simpleName}[$name]"
}