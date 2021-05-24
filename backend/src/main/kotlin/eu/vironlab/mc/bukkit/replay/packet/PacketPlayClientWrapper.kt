package eu.vironlab.mc.bukkit.replay.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.BlockPosition
import com.comphenix.protocol.wrappers.EnumWrappers


class WrappedPlayClientPosition(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {


    var x: Double
        get() = wrapped.doubles.read(0)
        set(value) {
            wrapped.doubles.write(0, value)
        }
    var y: Double
        get() = wrapped.doubles.read(1)
        set(value) {
            wrapped.doubles.write(1, value)
        }
    var z: Double
        get() = wrapped.doubles.read(2)
        set(value) {
            wrapped.doubles.write(2, value)
        }
    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Client.POSITION
    }
}

class WrappedPlayClientLook(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {


    var yaw: Float
        get() = wrapped.float.read(0)
        set(value) {
            wrapped.float.write(0, value)
        }
    var pitch: Float
        get() = wrapped.float.read(1)
        set(value) {
            wrapped.float.write(1, value)
        }
    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Client.LOOK
    }

}

class WrappedPlayClientPositionLook(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var x: Double
        get() = wrapped.doubles.read(0)
        set(value) {
            wrapped.doubles.write(0, value)
        }
    var y: Double
        get() = wrapped.doubles.read(1)
        set(value) {
            wrapped.doubles.write(1, value)
        }
    var z: Double
        get() = wrapped.doubles.read(2)
        set(value) {
            wrapped.doubles.write(2, value)
        }

    var yaw: Float
        get() = wrapped.float.read(0)
        set(value) {
            wrapped.float.write(0, value)
        }

    var pitch: Float
        get() = wrapped.float.read(1)
        set(value) {
            wrapped.float.write(1, value)
        }

    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Client.POSITION_LOOK
    }
}

class WrappedPlayClientEntityAction(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }
    var jumpBoost: Int
        get() = wrapped.integers.read(1)
        set(value) {
            wrapped.integers.write(1, value)
        }
    var action: EnumWrappers.PlayerAction
        get() = wrapped.playerActions.read(0)
        set(value) {
            wrapped.playerActions.write(0, value)
        }


    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Client.POSITION_LOOK
    }
}

class WrappedPlayClientBlockDig(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var location: BlockPosition
        get() = wrapped.blockPositionModifier.read(0)
        set(value) {
            wrapped.blockPositionModifier.write(0, value)
        }
    var direction: EnumWrappers.Direction
        get() = wrapped.directions.read(0)
        set(value) {
            wrapped.directions.write(0, value)
        }
    var status: EnumWrappers.PlayerDigType
        get() = wrapped.playerDigTypes.read(0)
        set(value) {
            wrapped.playerDigTypes.write(0, value)
        }

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Client.BLOCK_DIG
    }
}