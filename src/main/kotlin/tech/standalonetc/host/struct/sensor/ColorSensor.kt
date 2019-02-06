package tech.standalonetc.host.struct.sensor

import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dependency.NamedComponent
import tech.standalonetc.host.data.ColorSensorData
import tech.standalonetc.host.struct.tryPost
import java.util.concurrent.atomic.AtomicReference

class ColorSensor(name: String) : NamedComponent<ColorSensor>(name), Sensor<ColorSensorData> {

    private val value = AtomicReference(ColorSensorData(.0, .0, .0, .0))

    val r get() = value.get().r

    val g get() = value.get().g

    val b get() = value.get().b

    val a get() = value.get().a

    override val updated: ISource<ColorSensorData> = broadcast()

    override fun update(new: ColorSensorData) {
        if (this.value.getAndSet(new) != new)
            updated tryPost new
    }

    override fun toString(): String = "${javaClass.simpleName}[$name]"

}