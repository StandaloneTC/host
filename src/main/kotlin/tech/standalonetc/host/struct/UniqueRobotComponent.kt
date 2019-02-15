package tech.standalonetc.host.struct

import org.mechdancer.dependency.Component
import org.mechdancer.dependency.DependencyManager
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent

abstract class UniqueRobotComponent<T : UniqueRobotComponent<T>>
    : UniqueComponent<T>(), RobotComponent, Dependent {
    protected val manager = DependencyManager()
    override fun sync(dependency: Component): Boolean = manager.sync(dependency)

    override fun toString(): String = javaClass.simpleName.toLowerCase()

    fun String.joinPrefix() = "${toString()}.$this"
}