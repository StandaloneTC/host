package tech.standalonetc.host

import org.mechdancer.common.extension.log4j.loggerWrapper
import org.mechdancer.dependency.NamedComponent
import org.mechdancer.dependency.UniqueComponent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.standalonetc.host.struct.Device

fun ByteArray.decodeToBoolean() = when (firstOrNull()?.toInt()) {
    1 -> true
    0 -> false
    null -> null
    else -> throw IllegalArgumentException()
}


fun <T : Comparable<T>> T.checkedValue(range: ClosedFloatingPointRange<T>) =
    takeIf { it in range }

val loggingConfig = loggerWrapper {
    console()
    file()
}

val logger: Logger = LoggerFactory.getLogger("Robot").also(loggingConfig)

fun deviceBundle(block: DeviceBundle.() -> Unit) = DeviceBundle().apply(block)

fun Robot.setupDeviceBundle(deviceBundle: DeviceBundle) =
    deviceBundle.devices.forEach { (_, component) ->
        if (component !is Device)
            setup(component)
        else devices.add(component)
    }

fun Robot.setupDeviceBundle(block: DeviceBundle.() -> Unit) = setupDeviceBundle(deviceBundle(block))

fun Robot.setupDeviceBundleAndInit(deviceBundle: DeviceBundle, oppositeTimeout: Long = Long.MAX_VALUE) {
    setupDeviceBundle(deviceBundle)
    init(*deviceBundle.idMapping.toList().toTypedArray(), oppositeTimeout = oppositeTimeout)
}

fun Robot.setupDeviceBundleAndInit(block: DeviceBundle.() -> Unit, oppositeTimeout: Long = Long.MAX_VALUE) =
    setupDeviceBundleAndInit(deviceBundle(block), oppositeTimeout)

fun NamedComponent<*>.joinPrefix(name: String) = "${type.simpleName!!}[$name].$name"

fun UniqueComponent<*>.joinPrefix(name: String) = "${type.simpleName!!.toLowerCase()}.$name"

