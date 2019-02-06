package tech.standalonetc.host

import org.apache.log4j.Level
import org.mechdancer.common.extension.log4j.Locate
import org.mechdancer.common.extension.log4j.console
import org.mechdancer.common.extension.log4j.logger
import org.mechdancer.dependency.NamedComponent
import org.mechdancer.dependency.UniqueComponent
import org.slf4j.Logger

fun ByteArray.decodeToBoolean() = when (firstOrNull()?.toInt()) {
    1 -> true
    0 -> false
    null -> null
    else -> throw IllegalArgumentException()
}

fun withTimeout(timeout: Long, block: () -> Boolean): Boolean {
    if (timeout <= 0L) throw IllegalArgumentException("Timed out immediately")
    val start = System.currentTimeMillis()
    var result = false
    while (System.currentTimeMillis() - start < timeout
        && !block().also { result = it }
    );
    return result
}

fun <T : Comparable<T>> T.checkedValue(range: ClosedFloatingPointRange<T>) =
    takeIf { it in range }

val logger: Logger = logger("Robot") {
    level = Level.ALL
    console(Locate)
    file()
}

fun deviceBundle(block: DeviceBundle.() -> Unit) = DeviceBundle().apply(block)

fun Robot.setupDeviceBundle(deviceBundle: DeviceBundle) =
    deviceBundle.devices.forEach { (_, device) -> setup(device) }

fun Robot.setupDeviceBundle(block: DeviceBundle.() -> Unit) = setupDeviceBundle(deviceBundle(block))

fun Robot.setupDeviceBundleAndInit(deviceBundle: DeviceBundle) {
    setupDeviceBundle(deviceBundle)
    init(*deviceBundle.idMapping.toList().toTypedArray())
}

fun Robot.setupDeviceBundleAndInit(block: DeviceBundle.() -> Unit) = setupDeviceBundleAndInit(deviceBundle(block))

fun NamedComponent<*>.joinPrefix(name: String) = "${type.simpleName!!}[$name].$name"

fun UniqueComponent<*>.joinPrefix(name: String) = "${type.simpleName!!.toLowerCase()}.$name"

