package tech.standalonetc.host.struct

/**
 * Abstract device
 *
 * It is a component with a name, can be added to a scope.
 */
interface Device : RobotComponent {

    val name: String

}
