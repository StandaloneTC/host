package tech.standalonetc.host

import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class RobotProgramRunner<T : Robot>(
    loggingNetwork: Boolean = false,
    loggingRemoteHub: Boolean = false,
    clazz: KClass<T>
) : Closeable {

    private val current = AtomicReference<RobotProgram<T>?>(null)

    companion object {
        inline operator fun <reified T : Robot> invoke() =
            RobotProgramRunner(clazz = T::class)
    }

    @Suppress("UNCHECKED_CAST")
    private val robot =
        clazz.objectInstance
            ?: clazz.java.getConstructor(Boolean::class.java, Boolean::class.java).newInstance(
                loggingNetwork,
                loggingRemoteHub
            )


    fun <R : RobotProgram<T>> switchProgram(robotProgram: KClass<R>) {
        robotProgram.java.getConstructor().newInstance().let { new ->
            RobotProgram::class.java.getDeclaredField("robot").also { it.isAccessible = true }.set(new, robot)
            current.getAndSet(new)?.close()
            robot.close()
            robot.init()
            new.run()
        }
    }

    override fun close() {
        current.getAndSet(null)?.close()
        robot.close()
    }

}