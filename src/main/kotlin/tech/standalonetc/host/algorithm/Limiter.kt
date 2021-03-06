package tech.standalonetc.host.algorithm

import java.lang.Math.abs

class Limiter(min: Double, max: Double) {
	private val min: Double
	private val max: Double

	init {
		if (min <= max) {
			this.min = min
			this.max = max
		} else throw IllegalArgumentException("最小值比最大值大！")
	}

	constructor(max: Double = 1.0) : this(-abs(max), abs(max))

	operator fun invoke(data: Double) = if (data < min) min else if (data > max) max else data
}