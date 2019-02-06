package tech.standalonetc.host.struct

import org.mechdancer.dependency.Component
import org.mechdancer.dependency.DynamicScope

/**
 * RobotComponent
 *
 * Robot component has its lifecycle: [init], [stop].
 */
interface RobotComponent : Component {

    fun init() {}

    fun stop() {}

}
