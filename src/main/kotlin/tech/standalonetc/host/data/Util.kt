package tech.standalonetc.host.data

import tech.standalonetc.protocol.RobotPacket

fun RobotPacket.GamepadDataPacket.toGamepadData() = GamepadData(
    leftBumper,
    rightBumper,
    aButton,
    bButton,
    xButton,
    yButton,
    upButton,
    downButton,
    leftButton,
    rightButton,
    leftStickX,
    leftStickY,
    leftStickButton,
    rightStickX,
    rightStickY,
    rightStickButton,
    leftTrigger,
    rightTrigger
)

fun RobotPacket.EncoderDataPacket.toEncoderData() = EncoderData(position.toDouble(), speed)

fun RobotPacket.GyroDataPacket.toGyroData() = GyroData(pitchRate, yawRate, rollRate)

fun RobotPacket.ColorSensorDataPacket.toColorSensorData() = ColorSensorData(r, g, b, a)

fun RobotPacket.OpModeInfoPacket.toOpModeState() = OpModeState.values().find { it.code == state }!!