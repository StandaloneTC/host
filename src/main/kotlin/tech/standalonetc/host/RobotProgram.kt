package tech.standalonetc.host

import org.mechdancer.dataflow.core.linkTo
import org.mechdancer.dependency.Component
import org.mechdancer.dependency.DependencyManager
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import tech.standalonetc.host.data.OpModeState
import tech.standalonetc.host.struct.RobotComponent

abstract class RobotProgram<T : Robot>
    : UniqueComponent<RobotProgram<T>>(),
    RobotComponent,
    Dependent {

    protected lateinit var robot: T
        private set

    protected val manager = DependencyManager()

    final override fun sync(dependency: Component): Boolean = manager.sync(dependency)

    internal fun hookOpMode() {
        robot.opModeState linkTo {
            when (it) {
                OpModeState.Init -> onOpModeInit()
                OpModeState.Start -> onOpModeStart()
                OpModeState.Stop -> onOpModeStop()
            }
        }
    }


    protected open fun onOpModeInit() {

    }

    protected open fun onOpModeStart() {

    }

    protected open fun onOpModeStop() {

    }


}