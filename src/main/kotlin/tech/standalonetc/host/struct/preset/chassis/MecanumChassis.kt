package tech.standalonetc.host.struct.preset.chassis

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dataflow.core.post
import org.mechdancer.dependency.*
import tech.standalonetc.host.struct.effector.Motor

open class MecanumChassis : UniqueComponent<MecanumChassis>(), Dependent, Chassis {
    private val manager = DependencyManager()

    private val lf by manager.must<Motor>("chassis.LF")
    private val lb by manager.must<Motor>("chassis.LB")
    private val rf by manager.must<Motor>("chassis.RF")
    private val rb by manager.must<Motor>("chassis.RB")

    val descartesControl = broadcast<Descartes>()

    override var maxPower = 1.0

    override fun sync(dependency: Component): Boolean = manager.sync(dependency)

    override fun init() {
        descartesControl - {
            with(it) {
                doubleArrayOf(
                    x + y - w,
                    x - y - w,
                    x - y + w,
                    x + y + w
                )
            }.standardizeBy(maxPower)
        } - {
            lf.power post it[0]
            lb.power post it[1]
            rf.power post it[2]
            rb.power post it[3]
        }
    }

    data class Descartes(val x: Double, val y: Double, val w: Double)

    override fun toString(): String = javaClass.simpleName

}
