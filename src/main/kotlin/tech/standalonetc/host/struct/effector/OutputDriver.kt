package tech.standalonetc.host.struct.effector

import org.mechdancer.common.concurrent.RestartableTimer
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.linkTo
import org.mechdancer.dataflow.core.post
import tech.standalonetc.host.DataBlock
import java.io.Closeable

class OutputDriver<T>(private val transform: ((T) -> T?) = { it }) : DataBlock<T> by broadcast(), Closeable {


    private val timer = RestartableTimer()

    init {
        this linkTo {
            timer(100) {
                this@OutputDriver post this@OutputDriver.receive()
            }
        }
    }


    infix fun linkWithTransform(block: (T) -> Unit) = linkTo {
        transform(it)?.let(block)
    }

    override fun close() {
        timer.close()
    }
}