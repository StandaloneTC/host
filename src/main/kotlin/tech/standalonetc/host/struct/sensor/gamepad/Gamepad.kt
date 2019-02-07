package tech.standalonetc.host.struct.sensor.gamepad

import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.intefaces.ISource
import tech.standalonetc.host.data.GamepadData
import tech.standalonetc.host.struct.TreeComponent
import tech.standalonetc.host.struct.sensor.Sensor
import tech.standalonetc.host.struct.sensor.gamepad.Stick.Coordinate

/**
 * Gamepad
 */
class Gamepad(index: Int) : TreeComponent("gamepad$index", null),
    Sensor<GamepadData> {
    //==============
    // Top area
    //==============
    /** Left bumper */
    val leftBumper = button("left bumper")
    /** Right bumper */
    val rightBumper = button("right bumper")

    //==============
    // Button area
    //==============
    /** Button a */
    val a = button("A")
    /** Button b */
    val b = button("B")
    /** Button x */
    val x = button("X")
    /** Button y */
    val y = button("Y")

    //==============
    // Arrow area
    //==============
    /** Arrow up */
    val up = button("up")
    /** Arrow down */
    val down = button("down")
    /** Arrow left */
    val left = button("left")
    /** Arrow right */
    val right = button("right")

    //==============
    // Stick area
    //==============
    /** Left stick */
    val leftStick = stick("left stick")
    /** Right stick */
    val rightStick = stick("right stick")

    //==============
    // Trigger area
    //==============
    /** Left trigger */
    val leftTrigger = trigger("left trigger")
    /** Right trigger */
    val rightTrigger = trigger("right trigger")

    override val updated: ISource<GamepadData> = broadcast()

    override fun update(new: GamepadData) {
        leftBumper.update(new.leftBumper)
        rightBumper.update(new.rightBumper)
        a.update(new.aButton)
        b.update(new.bButton)
        x.update(new.xButton)
        y.update(new.yButton)
        up.update(new.upButton)
        down.update(new.downButton)
        left.update(new.leftButton)
        right.update(new.rightButton)
        with(new) {
            leftStick.update(
                Stick.Value(
                    Coordinate(leftStickX, leftStickY),
                    leftStickButton
                )
            )
            rightStick.update(
                Stick.Value(
                    Coordinate(rightStickX, rightStickY),
                    rightStickButton
                )
            )
        }
        leftTrigger.update(new.leftTrigger)
        rightTrigger.update(new.rightTrigger)
    }

    private fun button(name: String) = Button(name, this).asChild()
    private fun stick(name: String) = Stick(name, this).asChild()
    private fun trigger(name: String) = Trigger(name, this).asChild()
}
