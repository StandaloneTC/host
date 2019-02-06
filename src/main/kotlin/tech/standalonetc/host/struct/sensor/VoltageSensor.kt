package tech.standalonetc.host.struct.sensor

import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dependency.UniqueComponent
import tech.standalonetc.host.struct.tryPost
import java.util.concurrent.atomic.AtomicReference

/**
 * Voltage sensor
 */
class VoltageSensor : UniqueComponent<VoltageSensor>(), Sensor<Double> {

    private val _voltage = AtomicReference(.0)
    override val name: String = "voltage"

    val voltage: Double get() = _voltage.get()

    override val updated: ISource<Double> = broadcast()

    override fun update(new: Double) {
        if (_voltage.getAndSet(new) != new)
            updated tryPost new
    }

    override fun toString(): String = "VoltageSensor"

}
