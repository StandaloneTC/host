package tech.standalonetc.host

import org.mechdancer.dependency.NamedComponent
import tech.standalonetc.host.struct.Device
import tech.standalonetc.host.struct.effector.ContinuousServo
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.effector.MotorWithEncoder
import tech.standalonetc.host.struct.effector.Servo
import tech.standalonetc.host.struct.sensor.ColorSensor
import tech.standalonetc.host.struct.sensor.Encoder
import tech.standalonetc.host.struct.sensor.Gyro
import tech.standalonetc.host.struct.sensor.TouchSensor
import java.util.concurrent.atomic.AtomicInteger

open class DeviceBundle {

    private val id = AtomicInteger(0)

    @PublishedApi
    internal val devices = mutableMapOf<Byte, NamedComponent<*>>()

    val idMapping
        get() = devices.entries.associate { (id, device) -> device.name to id }

    private fun <T : NamedComponent<T>> T.add() = apply {
        if (id.get() >= 127)
            throw RuntimeException("Too many devices.")
        devices[id.getAndIncrement().toByte()] = this
    }

    inner class NamedScope(private val prefix: String) {

        private fun named(name: String) = "$prefix.$name"

        fun motor(name: String, direction: Motor.Direction = Motor.Direction.FORWARD) =
            this@DeviceBundle.motor(named(name), direction)

        fun servo(name: String, range: ClosedFloatingPointRange<Double>) =
            this@DeviceBundle.servo(named(name), range)

        fun continuousServo(name: String) =
            this@DeviceBundle.continuousServo(named(name))

        fun colorSensor(name: String) =
            this@DeviceBundle.colorSensor(named(name))

        fun encoder(name: String, cpr: Double = 360.0) =
            this@DeviceBundle.encoder(named(name), cpr)

        fun gyro(name: String) =
            this@DeviceBundle.gyro(named(name))

        fun touchSensor(name: String) =
            this@DeviceBundle.touchSensor(named(name))

        fun motorWithEncoder(
            name: String,
            direction: Motor.Direction = Motor.Direction.FORWARD,
            cpr: Double = 360.0,
            controller: (Double) -> Double = { it }
        ) = this@DeviceBundle.motorWithEncoder(named(name), direction, cpr, controller)

        operator fun String.invoke(block: NamedScope.() -> Unit) = withPrefix(this, block)

        fun withPrefix(first: String, block: NamedScope.() -> Unit) =
            NamedScope(named("") + first).apply(block)
    }

    fun withPrefix(prefix: String, block: NamedScope.() -> Unit) =
        NamedScope(prefix).apply(block)

    operator fun String.invoke(block: NamedScope.() -> Unit) = withPrefix(this, block)

    fun motor(name: String, direction: Motor.Direction = Motor.Direction.FORWARD) =
        Motor(name, direction).add()

    fun servo(name: String, range: ClosedFloatingPointRange<Double>) =
        Servo(name, range).add()

    fun continuousServo(name: String) =
        ContinuousServo(name).add()

    fun colorSensor(name: String) =
        ColorSensor(name).add()

    fun encoder(name: String, cpr: Double = 360.0) =
        Encoder(name, cpr).add()

    fun gyro(name: String) =
        Gyro(name).add()

    fun touchSensor(name: String) =
        TouchSensor(name).add()

    fun motorWithEncoder(
        name: String,
        direction: Motor.Direction = Motor.Direction.FORWARD,
        cpr: Double = 360.0,
        controller: (Double) -> Double
    ) {
        Motor(name, direction).add()
        Encoder(name, cpr).add()
        MotorWithEncoder(name, controller).add()
    }

    inline fun <reified T : Device> findByName(name: String) = lazy {
        devices.values.find {
            it is T && it.name == name
        } ?: throw RuntimeException("Can not find $name:${T::class.simpleName}")
    }

}
