package tech.standalonetc.host

import org.mechdancer.common.extension.cast
import org.mechdancer.common.extension.safeCast
import java.lang.reflect.ParameterizedType
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class RobotProgramContainer<T : Robot>(
    loggingNetwork: Boolean = false,
    loggingRemoteHub: Boolean = false
) {

    private val current = AtomicReference<RobotProgram<T>>(null)

    @Suppress("UNCHECKED_CAST")
    private val robot =
        javaClass.genericSuperclass
            .safeCast<ParameterizedType>()
            ?.let { type ->
                type.actualTypeArguments.find { t -> t is Class<*> && Robot::class.java.isAssignableFrom(t) }
            }
            ?.let { it.cast<Class<*>>() }
            ?.let {
                runCatching {
                    it.kotlin.objectInstance
                        ?: it.getConstructor(Boolean::class.java, Boolean::class.java).newInstance(
                            loggingNetwork,
                            loggingRemoteHub
                        )
                }.onFailure {
                    when (it) {
                        is NoSuchMethodException -> throw IllegalArgumentException("Unable to find public constructor.")
                        is InstantiationException -> throw IllegalArgumentException("Robot can not be abstract.")
                    }
                }.getOrNull()
            } as? T ?: throw IllegalArgumentException("Unable to find generic parameter.")

    fun switchProgram(robotProgram: KClass<RobotProgram<T>>) {
        robotProgram.java.getConstructor(Robot::class.java).newInstance(robot).let { new ->
            current.getAndSet(new).close()
            robot.close()
            robot.init()
            new.run()
        }
    }
}