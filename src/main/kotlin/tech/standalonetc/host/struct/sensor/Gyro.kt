package tech.standalonetc.host.struct.sensor

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.intefaces.ISource
import org.mechdancer.dependency.NamedComponent
import tech.standalonetc.host.data.GyroData
import tech.standalonetc.host.struct.post
import java.util.concurrent.atomic.AtomicReference

/**
 * Gyro
 */
class Gyro(name: String) : NamedComponent<Gyro>(name), Sensor<GyroData> {

    private val value = AtomicReference(GyroData(.0, .0, .0))

    val pitchRate
        get() = value.get().pitchRate

    val yawRate
        get() = value.get().yawRate

    val rollRate
        get() = value.get().rollRate

    override val updated: ISource<GyroData> = broadcast()

    override fun update(new: GyroData) {
        if (this.value.getAndSet(new) != new)
            updated post new
    }

    override fun toString(): String = "${javaClass.simpleName}[$name]"

}
