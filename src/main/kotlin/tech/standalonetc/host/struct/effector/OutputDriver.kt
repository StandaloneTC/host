package tech.standalonetc.host.struct.effector

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mechdancer.dataflow.blocks.IBroadcastBlock
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.linkTo
import org.mechdancer.dataflow.core.post
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

class OutputDriver<T>(private val transform: ((T) -> T?) = { it }) : IBroadcastBlock<T> by broadcast(), Closeable {

    private val ref = AtomicReference<Job?>(null)

    init {
        this linkTo {
            ref.getAndUpdate { last ->
                if (last?.isCancelled == false)
                    last.cancel()
                createCoroutine()
            }
        }
    }

    private fun createCoroutine(): Job =
        GlobalScope.launch {
            delay(100)
            this@OutputDriver post this@OutputDriver.receive()
            ref.getAndUpdate {
                if (it == null || it.isCancelled) createCoroutine()
                else it
            }
        }

    infix fun linkWithTransform(block: (T) -> Unit) = linkTo {
        transform(it)?.let(block)
    }

    override fun close() {
        ref.get()?.cancel()
    }
}