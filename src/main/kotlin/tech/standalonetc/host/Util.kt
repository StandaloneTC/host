package tech.standalonetc.host

import org.mechdancer.common.extension.log4j.loggerWrapper
import org.mechdancer.dataflow.core.intefaces.IFullyBlock
import org.mechdancer.dataflow.core.intefaces.ILink
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.standalonetc.host.struct.Device

fun ByteArray.decodeToBoolean() = when (firstOrNull()?.toInt()) {
    1    -> true
    0    -> false
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

fun Robot.setupDeviceBundle(deviceBundle: DeviceBundle) {
    deviceBundle.devices.forEach { (_, component) ->
        if (component !is Device)
            setup(component)
        else devices.add(component)
        setIdMapping(*deviceBundle.idMaps)
    }
}

fun Robot.setupDeviceBundle(block: DeviceBundle.() -> Unit) = setupDeviceBundle(deviceBundle(block))

fun Robot.setupDeviceBundleAndInit(deviceBundle: DeviceBundle, oppositeTimeout: Long = Long.MAX_VALUE) {
    setupDeviceBundle(deviceBundle)
    init(oppositeTimeout)
}

fun Robot.setupDeviceBundleAndInit(block: DeviceBundle.() -> Unit, oppositeTimeout: Long = Long.MAX_VALUE) =
    setupDeviceBundleAndInit(deviceBundle(block), oppositeTimeout)

val DeviceBundle.idMaps
    get() = idMapping.toList().toTypedArray()

fun breakAllConnections() = ILink.list.forEach {
    it.dispose()
}

typealias DataBlock<T> = IFullyBlock<T, T>