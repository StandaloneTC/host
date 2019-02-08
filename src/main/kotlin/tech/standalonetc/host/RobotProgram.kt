package tech.standalonetc.host

import org.mechdancer.dataflow.core.linkTo
import tech.standalonetc.host.data.OpModeState
import java.io.Closeable

abstract class RobotProgram<T : Robot>(protected val robot: T) : Runnable, Closeable {

    init {
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

    override fun close() {
    }

}