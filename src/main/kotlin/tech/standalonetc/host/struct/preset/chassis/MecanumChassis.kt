package tech.standalonetc.host.struct.preset.chassis

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.linkTo
import org.mechdancer.dataflow.core.minus
import org.mechdancer.dataflow.core.post
import org.mechdancer.dependency.Component
import org.mechdancer.dependency.must
import tech.standalonetc.host.struct.UniqueRobotComponent
import tech.standalonetc.host.struct.effector.Motor

open class MecanumChassis(name: String = "chassis")
    : UniqueRobotComponent<MecanumChassis>(), Chassis {

    private val lf by manager.must<Motor>("$name.LF")
    private val lb by manager.must<Motor>("$name.LB")
    private val rf by manager.must<Motor>("$name.RF")
    private val rb by manager.must<Motor>("$name.RB")

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
            }.standardizeByMaxPower()
        } linkTo {
            lf.power post it[0]
            lb.power post it[1]
            rf.power post it[2]
            rb.power post it[3]
        }
    }

    data class Descartes(val x: Double, val y: Double, val w: Double)
}
