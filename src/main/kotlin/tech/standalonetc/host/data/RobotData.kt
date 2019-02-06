package tech.standalonetc.host.data

import kotlin.random.Random


data class EncoderData(val position: Double, val speed: Double)

data class GamepadData(
    val leftBumper: Boolean,
    val rightBumper: Boolean,
    val aButton: Boolean,
    val bButton: Boolean,
    val xButton: Boolean,
    val yButton: Boolean,
    val upButton: Boolean,
    val downButton: Boolean,
    val leftButton: Boolean,
    val rightButton: Boolean,
    val leftStickX: Double,
    val leftStickY: Double,
    val leftStickButton: Boolean,
    val rightStickX: Double,
    val rightStickY: Double,
    val rightStickButton: Boolean,
    val leftTrigger: Double,
    val rightTrigger: Double
) {
    companion object {
        fun random() = GamepadData(
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextBoolean(),
            Random.nextDouble(-1.0, 1.0),
            Random.nextDouble(-1.0, 1.0),
            Random.nextBoolean(),
            Random.nextDouble(-1.0, 1.0),
            Random.nextDouble(-1.0, 1.0),
            Random.nextBoolean(),
            Random.nextDouble(.0, 1.0),
            Random.nextDouble(.0, 1.0)
        )
    }
}

data class ColorSensorData(
    val r: Double,
    val g: Double,
    val b: Double,
    val a: Double
)

data class GyroData(
    val pitchRate: Double,
    val yawRate: Double,
    val rollRate: Double
)