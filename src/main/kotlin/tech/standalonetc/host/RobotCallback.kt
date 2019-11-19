package tech.standalonetc.host

import org.mechdancer.dataflow.core.intefaces.ILink
import org.mechdancer.dataflow.core.linkTo
import tech.standalonetc.host.data.OpModeState

abstract class RobotCallback<T : Robot> {

    private var opModeStateHook: ILink<OpModeState>? = null

    internal fun hookOpMode(robot: T) {
        opModeStateHook?.close()
        opModeStateHook = robot.opModeState linkTo {
            when (it) {
                OpModeState.Init  -> onOpModeInit(robot)
                OpModeState.Start -> onOpModeStart(robot)
                OpModeState.Stop  -> onOpModeStop(robot)
            }
        }
    }


    abstract fun init(robot: T)

    abstract fun stop(robot: T)


    protected open fun onOpModeInit(robot: T) {

    }

    protected open fun onOpModeStart(robot: T) {

    }

    protected open fun onOpModeStop(robot: T) {

    }

}