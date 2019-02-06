package tech.standalonetc.host.struct

import org.mechdancer.dataflow.core.IPostable
import org.mechdancer.dataflow.core.ISource
import org.mechdancer.dataflow.core.post
import org.mechdancer.dependency.Component
import org.mechdancer.dependency.DynamicScope

/**
 * Add a tree component [root] to scope recursively.
 */
fun DynamicScope.setupRecursive(root: TreeComponent) {
    setup(root)
    root.children.forEach(this::setupRecursive)
}

/**
 * Try casting a source into postable and post a [value].
 *
 * @receiver source
 */
@Suppress("UNCHECKED_CAST")
infix fun <T> ISource<T>.tryPost(value: T) =
    (this as? IPostable<T>)?.post(value)


/**
 * Find components in collection whose type is [T]
 */
inline fun <reified T : Component> Collection<Component>.find() = mapNotNull { it as? T }

/**
 * Find all devices in a component collection
 */
fun Collection<Component>.findAllDevices() = find<Device>()

/**
 * Associate a device collection by theirs name
 */
fun <T : Device> Collection<T>.mapWithName() = associateBy { it.name }
