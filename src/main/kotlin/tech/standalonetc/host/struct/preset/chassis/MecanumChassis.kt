package tech.standalonetc.host.struct.preset.chassis

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dataflow.core.post
import org.mechdancer.dependency.Component
import org.mechdancer.dependency.DependencyManager
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.must
import tech.standalonetc.host.struct.TreeComponent
import tech.standalonetc.host.struct.effector.Motor

open class MecanumChassis : TreeComponent("chassis", null), Dependent, Chassis {
    private val manager = DependencyManager()

    private val lf by manager.must<Motor>("LF".joinPrefix())
    private val lb by manager.must<Motor>("LB".joinPrefix())
    private val rf by manager.must<Motor>("RF".joinPrefix())
    private val rb by manager.must<Motor>("RB".joinPrefix())

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

    override fun toString(): String = "${javaClass.simpleName}[$name]"

}
