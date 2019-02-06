package tech.standalonetc.host.struct.preset.chassis

import tech.standalonetc.host.struct.RobotComponent
import kotlin.math.abs
import kotlin.math.sign


interface Chassis : RobotComponent {

    var maxPower: Double

    fun DoubleArray.standardizeBy(maxPower: Double) =
        map(::abs).max()!!.let {
            if (it <= abs(maxPower))
                maxPower.sign
            else
                maxPower / it
        }.let {
            DoubleArray(size) { i ->
                this[i] * it
            }
        }


}
