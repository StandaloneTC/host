package tech.standalonetc.host.struct.sensor.gamepad

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.intefaces.ISource
import tech.standalonetc.host.struct.TreeComponent
import tech.standalonetc.host.struct.sensor.Sensor
import tech.standalonetc.host.struct.post
import java.util.concurrent.atomic.AtomicReference

/**
 * Gamepad stick
 */
class Stick(name: String, gamepad: Gamepad) : TreeComponent(name, gamepad),
    Sensor<Stick.Value> {

    private val value = AtomicReference(Value(Coordinate(.0, .0), false))

    /** Is stick pressed */
    val pressed get() = value.get().pressed

    /** X value of the stick */
    val x get() = value.get().coordinate.x

    /** Y value of the stick */
    val y get() = value.get().coordinate.y

    /** Pressing event */
    val pressing: ISource<Unit> = broadcast()

    /** Releasing event */
    val releasing: ISource<Unit> = broadcast()

    /** coordinate value changed event */
    val valueChanged: ISource<Coordinate> = broadcast()

    override val updated: ISource<Value> = broadcast()

    override fun update(new: Value) {
        val last = this.value.getAndSet(new)
        if (last != new) {
            updated post new
            if (last.coordinate != new.coordinate) valueChanged post new.coordinate
            if (last.pressed != new.pressed) (if (new.pressed) pressing else releasing) post Unit
        }
    }

    data class Coordinate(val x: Double, val y: Double)
    data class Value(val coordinate: Coordinate, val pressed: Boolean)
}
