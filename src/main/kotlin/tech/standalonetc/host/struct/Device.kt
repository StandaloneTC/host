package tech.standalonetc.host.struct

/**
 * Real devices involved in communication
 *
 * It is a component with a name, can be added to a scope.
 */
interface Device : RobotComponent {

    /** Device name */
    val name: String

}
