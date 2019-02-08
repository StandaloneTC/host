package tech.standalonetc.host

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.mechdancer.common.concurrent.repeatWithTimeout
import org.mechdancer.dataflow.core.broadcast
import org.mechdancer.dataflow.core.post
import org.mechdancer.dependency.DynamicScope
import org.mechdancer.dependency.plusAssign
import tech.standalonetc.host.data.*
import tech.standalonetc.host.struct.Device
import tech.standalonetc.host.struct.RobotComponent
import tech.standalonetc.host.struct.effector.ContinuousServo
import tech.standalonetc.host.struct.effector.Motor
import tech.standalonetc.host.struct.effector.Servo
import tech.standalonetc.host.struct.find
import tech.standalonetc.host.struct.mapWithName
import tech.standalonetc.host.struct.sensor.*
import tech.standalonetc.host.struct.sensor.gamepad.Gamepad
import tech.standalonetc.protocol.RobotPacket
import tech.standalonetc.protocol.network.NetworkTools
import tech.standalonetc.protocol.network.PacketCallback
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet

open class Robot @JvmOverloads constructor(
    private val loggingNetwork: Boolean = false,
    private val loggingRemoteHub: Boolean = false
) : DynamicScope(), Closeable {

    val devices: MutableList<Device> = mutableListOf()

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
                this@Robot.opModeState post toOpModeState()
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
     * OpMode state
     */
    val opModeState = broadcast<OpModeState>()

    /**
     * Init without parameters
     *
     * Should implemented by the child.
     */
    open fun init() {
        throw NotImplementedError("Not implemented")
    }

    /**
     * Init host compute controller
     *
     * Repeated calls will produce an exception
     */
    fun init(vararg mapId: Pair<String, Byte>, oppositeTimeout: Long = Long.MAX_VALUE) {

        //Avoid repeatedly call
        if (initialized) throw IllegalStateException()

        val nameAndId: Map<String, Byte> = mapId.toMap()

        val namedDevices = devices.associateBy { it.name }

        //Save ids
        idMapping = nameAndId.entries.associate { (k, v) -> v to k }

        //Use for send device requests
        networkTools = NetworkTools("Host", "Robot", 2, 2, onPacketReceive = packetListener)
        networkTools.setPacketConversion(RobotPacket.Conversion)

        //Generate devices requests
        val request = namedDevices.map { (name, _) ->
            RobotPacket.DeviceDescriptionPacket(
                nameAndId[name] ?: throw IllegalArgumentException("Device $name is not mapped with id"), name
            )
        }

        logger.info("Devices need to request:")
        request.forEach {
            logger.info("id: ${it.deviceId} name: ${it.deviceName}")
        }


        //Wait opposite
        repeatWithTimeout(oppositeTimeout) {
            networkTools.askOppositeAddress()
        } ?: throw RuntimeException("Unable to find opposite.")

        logger.info("Find opposite.")

        //Send request in parallel
        val result =
            runBlocking {
                request.map {
                    async(Dispatchers.IO) {
                        namedDevices.getValue(it.deviceName) to (networkTools.sendPacket(it)?.decodeToBoolean()
                            ?: false)
                    }
                }.awaitAll()
            }.toMap()


        //Process result
        result.filterValues { !it }.forEach { (device, _) -> logger.error("Unable to obtain device $device") }
        availableDevices.addAll(result.filterValues { it }.keys)


        //Setup devices
        setupAvailableDevices()

        //Call init
        components.mapNotNull { it as? RobotComponent }.forEach(RobotComponent::init)

        //Initialize sensor accessors
        initSensors()

        //Link outputs
        linkOutputs(nameAndId)

        logger.info("Initialized.")
        initialized = true
    }


    /**
     * Init host computer controller
     *
     * Without require devices.
     */
    fun initWithoutWaiting(vararg mapId: Pair<String, Byte>) {

        //Avoid repeatedly call
        if (initialized) throw IllegalStateException()

        val nameAndId: Map<String, Byte> = mapId.toMap()

        val namedDevices = devices.associateBy { it.name }

        //Save ids
        idMapping = nameAndId.entries.associate { (k, v) -> v to k }

        initNetworkTools()

        //Check id
        namedDevices.forEach { (name, _) ->
            nameAndId[name] ?: throw IllegalArgumentException("Device $name is not mapped with id")
        }

        //Assume that all devices are available
        availableDevices.addAll(devices)

        //Setup devices
        setupAvailableDevices()

        //Call init
        components.mapNotNull { it as? RobotComponent }.forEach(RobotComponent::init)

        //Initialize sensor accessors
        initSensors()

        //Link outputs
        linkOutputs(nameAndId)

        logger.info("Initialized.")
        initialized = true
    }


    private fun initNetworkTools() {
        networkTools = NetworkTools(
            "Host",
            "Robot",
            2,
            2,
            onPacketReceive = packetListener,
            enableRemoteHubLogger = loggingRemoteHub,
            loggerConfig = {
                if (loggingNetwork)
                    loggingConfig(this)
            })
        networkTools.setPacketConversion(RobotPacket.Conversion)
    }

    private fun linkOutputs(nameAndId: Map<String, Byte>) {
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
    }

    private fun initSensors() {
        encoders = availableDevices.find<Encoder>().mapWithName()
        gyros = availableDevices.find<Gyro>().mapWithName()
        touches = availableDevices.find<TouchSensor>().mapWithName()
        colors = availableDevices.find<ColorSensor>().mapWithName()
    }

    private fun setupAvailableDevices() {
        availableDevices.forEach {
            this += it
        }
    }


    override fun close() {
        if (!initialized) return
        components.mapNotNull { it as? RobotComponent }.forEach(RobotComponent::stop)
        shutdownHook.forEach { it() }
        networkTools.close()
        availableDevices.clear()
    }

}
