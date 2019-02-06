package tech.standalonetc.host

import kotlinx.coroutines.*
import org.mechdancer.dependency.DynamicScope
import org.mechdancer.dependency.plusAssign
import tech.standalonetc.host.data.toColorSensorData
import tech.standalonetc.host.data.toEncoderData
import tech.standalonetc.host.data.toGamepadData
import tech.standalonetc.host.data.toGyroData
import tech.standalonetc.host.struct.*
import tech.standalonetc.host.struct.effector.ContinuousServo
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.effector.Servo
import tech.standalonetc.host.struct.sensor.*
import tech.standalonetc.host.struct.sensor.gamepad.Gamepad
import tech.standalonetc.protocol.RobotPacket
import tech.standalonetc.protocol.network.NetworkTools
import tech.standalonetc.protocol.network.PacketCallback
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet

class Robot(private val oppositeTimeout: Long = Long.MAX_VALUE) : DynamicScope(), Closeable {

    private lateinit var devices: Map<String, Device>

    private lateinit var idMapping: Map<Byte, String>


    private lateinit var networkTools: NetworkTools

    private val availableDevices: MutableList<Device> = mutableListOf()

    private var initialized = false



    //Sensors accessor
    private lateinit var encoders: Map<String, Encoder>
    private lateinit var gyros: Map<String, Gyro>
    private lateinit var touches: Map<String, TouchSensor>
    private lateinit var colors: Map<String, ColorSensor>


    private val shutdownHook = ConcurrentSkipListSet<() -> Unit> { o1, o2 -> o1.hashCode().compareTo(o2.hashCode()) }


    private val packetListener: PacketCallback = {
        when (this) {

            //Encoder
            is RobotPacket.EncoderDataPacket ->
                encoders[idMapping[id]]?.update(toEncoderData())

            //Gyro
            is RobotPacket.GyroDataPacket ->
                gyros[idMapping[id]]?.update(toGyroData())

            //TouchSensor
            is RobotPacket.TouchSensorDataPacket ->
                touches[idMapping[id]]?.update(bePressed)

            //ColorSensor
            is RobotPacket.ColorSensorDataPacket ->
                colors[idMapping[id]]?.update(toColorSensorData())

            //Gamepad
            is RobotPacket.GamepadDataPacket -> toGamepadData().let {
                when (id) {
                    RobotPacket.BuiltinId.GamepadMaster -> master.update(it)
                    RobotPacket.BuiltinId.GamepadHelper -> helper.update(it)
                }
            }

            //Environment
            is RobotPacket.VoltageDataPacket -> voltageSensor.update(voltage)
            is RobotPacket.OperationPeriodPacket -> this@Robot.period = period
            is RobotPacket.OpModeInfoPacket -> {
                this@Robot.opModeName = opModeName
                this@Robot.opModeState = opModeState
            }

        }
    }


    /**
     * Master gamepad
     */
    val master = Gamepad(0)

    /**
     * Helper gamepad
     */
    val helper = Gamepad(1)

    /**
     * Voltage sensor of battery
     */
    val voltageSensor = VoltageSensor()

    /**
     * Robot loop period
     */
    @Volatile
    var period = 0
        private set

    /**
     * Current opMode name
     */
    var opModeName = ""
        private set

    /**
     * Current opMode state
     */
    var opModeState = RobotPacket.OpModeInfoPacket.STOP
        private set

    /**
     * Init host compute controller
     *
     * Repeated calls will produce an exception
     */
    fun init(vararg mapId: Pair<String, Byte>) {

        val nameAndId: Map<String, Byte> = mapId.toMap()

        //Avoid repeatedly call
        if (initialized) throw IllegalStateException()

        devices = components.findAllDevices().associateBy { it.name }

        //Save ids
        idMapping = nameAndId.entries.associate { (k, v) -> v to k }

        //Use for send device requests
        networkTools = NetworkTools("Host", "Robot", 2, 2, onPacketReceive = packetListener)
        networkTools.setPacketConversion(RobotPacket.Conversion)

        //Generate devices requests
        val request = devices.map { (name, _) ->
            RobotPacket.DeviceDescriptionPacket(
                nameAndId[name] ?: throw IllegalArgumentException("Device $name is not mapped with id"), name
            )
        }

        logger.info("Devices need to request: \n${request.joinToString(separator = "\n") {
            "[${it.deviceId}:${it.deviceName}]"
        }
        }")

        //Wait opposite
        runBlocking {
            var job: Job? = null
            kotlinx.coroutines.withTimeout(oppositeTimeout) {
                job = GlobalScope.launch(Dispatchers.IO) {
                    networkTools.findOpposite()
                }
                job?.join()
            }
            job?.cancel()
            if (job?.isCompleted != true)
                throw  RuntimeException("Unable to find opposite.")
        }


        logger.info("Find opposite.")

        //Send request in parallel
        val result =
            runBlocking {
                request.map {
                    async(Dispatchers.IO) {
                        devices.getValue(it.deviceName) to (networkTools.sendPacket(it)?.decodeToBoolean() ?: false)
                    }
                }.awaitAll()
            }.toMap()


        //Process result
        result.filterValues { !it }.forEach { (device, _) -> logger.error("Unable to obtain device $device") }
        availableDevices.addAll(result.filterValues { it }.keys)


        //Configuration components' relationship
        components.forEach {
            if ((it is Device && it in availableDevices) || it !is Device)
                this += it
        }

        //Call init
        components.mapNotNull { it as? RobotComponent }.forEach(RobotComponent::init)

        //Initialize sensor accessors
        encoders = availableDevices.find<Encoder>().mapWithName()
        gyros = availableDevices.find<Gyro>().mapWithName()
        touches = availableDevices.find<TouchSensor>().mapWithName()
        colors = availableDevices.find<ColorSensor>().mapWithName()

        //Link output

        logger.info("Linking Motors")
        availableDevices.find<Motor>().forEach { device ->
            shutdownHook.add { device.power.close() }
            device.power linkWithTransform {
                networkTools.broadcastPacket(
                    RobotPacket.MotorPowerPacket(nameAndId.getValue(device.name), it)
                )
            }
        }

        logger.info("Linking Servos")
        availableDevices.find<Servo>().forEach { device ->
            shutdownHook.add { device.position.close() }
            device.position linkWithTransform {
                networkTools.broadcastPacket(
                    RobotPacket.ServoPositionPacket(nameAndId.getValue(device.name), it)
                )
            }
            shutdownHook.add { device.pwmEnable.close() }
            device.pwmEnable linkWithTransform {
                networkTools.broadcastPacket(
                    RobotPacket.PwmEnablePacket(nameAndId.getValue(device.name), it)
                )
            }
        }
        logger.info("Linking CRServos")
        availableDevices.find<ContinuousServo>().forEach { device ->
            shutdownHook.add { device.power.close() }
            device.power linkWithTransform {
                networkTools.broadcastPacket(
                    RobotPacket.ContinuousServoPowerPacket(nameAndId.getValue(device.name), it)
                )
            }
            shutdownHook.add { device.pwmEnable.close() }
            device.pwmEnable linkWithTransform {
                networkTools.broadcastPacket(
                    RobotPacket.PwmEnablePacket(nameAndId.getValue(device.name), it)
                )
            }
        }

        logger.info("Initialized.")
        initialized = true
    }

    override fun close() {
        components.mapNotNull { it as? RobotComponent }.forEach(RobotComponent::stop)
        shutdownHook.forEach { it() }
        networkTools.close()
        availableDevices.clear()
    }

}
