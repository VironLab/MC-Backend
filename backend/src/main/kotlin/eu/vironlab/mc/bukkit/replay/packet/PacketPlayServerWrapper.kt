package eu.vironlab.mc.bukkit.replay.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import java.util.*
import org.bukkit.World

class WrappedPlayServerEntityTeleport(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
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
        get() = wrapped.doubles.read(1)
        set(value) {
            wrapped.doubles.write(1, value)
        }

    var yaw: Float
        get() = ((wrapped.bytes.read(0) * 360) / 256).toFloat()
        set(value) {
            wrapped.bytes.write(0, (value * 256 / 260).toByte())
        }

    var pitch: Float
        get() = ((wrapped.bytes.read(1) * 360) / 256).toFloat()
        set(value) {
            wrapped.bytes.write(1, (value * 256 / 260).toByte())
        }

    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }


    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.REL_ENTITY_MOVE_LOOK
    }
}


class WrappedPlayServerRelEntityMoveLook(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }

    var dX: Int
        get() = wrapped.integers.read(1)
        set(value) {
            wrapped.integers.write(1, value)
        }
    var dY
        get() = wrapped.integers.read(2)
        set(value) {
            wrapped.integers.write(2, value)
        }
    var dZ
        get() = wrapped.integers.read(3)
        set(value) {
            wrapped.integers.write(3, value)
        }

    var yaw: Float
        get() = ((wrapped.bytes.read(0) * 360) / 256).toFloat()
        set(value) {
            wrapped.bytes.write(0, (value * 256 / 260).toByte())
        }

    var pitch: Float
        get() = ((wrapped.bytes.read(1) * 360) / 256).toFloat()
        set(value) {
            wrapped.bytes.write(1, (value * 256 / 260).toByte())
        }

    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }


    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.REL_ENTITY_MOVE_LOOK
    }
}


class WrappedPlayServerRelEntityMove(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }

    var dX: Int
        get() = wrapped.integers.read(1)
        set(value) {
            wrapped.integers.write(1, value)
        }
    var dY
        get() = wrapped.integers.read(2)
        set(value) {
            wrapped.integers.write(2, value)
        }
    var dZ
        get() = wrapped.integers.read(3)
        set(value) {
            wrapped.integers.write(3, value)
        }

    var onGround: Boolean
        get() = wrapped.booleans.read(0)
        set(value) {
            wrapped.booleans.write(0, value)
        }


    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.REL_ENTITY_MOVE
    }
}

class WrappedPlayServerSpawnEntityLiving(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }

    var uniqueId: UUID
        get() = wrapped.uuiDs.read(0)
        set(value) {
            wrapped.uuiDs.write(0, value)
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
        get() = (wrapped.bytes.read(0) * 360) / 256.toFloat()
        set(value) {
            wrapped.bytes.write(0, (value * 256 / 360).toInt().toByte())
        }

    var pitch: Float
        get() = (wrapped.bytes.read(1) * 360) / 256.toFloat()
        set(value) {
            wrapped.bytes.write(1, (value * 256 / 360).toInt().toByte())
        }

    var headPitch: Float
        get() = (wrapped.bytes.read(2) * 360) / 256.toFloat()
        set(value) {
            wrapped.bytes.write(2, (value * 256 / 360).toInt().toByte())
        }

    var velocityX: Double
        get() = wrapped.integers.read(2) / 8000.0
        set(value) {
            wrapped.integers.write(2, (value * 8000).toInt())
        }

    var velocityY: Double
        get() = wrapped.integers.read(3) / 8000.0
        set(value) {
            wrapped.integers.write(3, (value * 8000).toInt())
        }

    var velocityZ: Double
        get() = wrapped.integers.read(4) / 8000.0
        set(value) {
            wrapped.integers.write(4, (value * 8000).toInt())
        }

    var entityType: Int
        get() = wrapped.integers.read(1)
        set(value) {
            wrapped.integers.write(1, value)
        }

    var objectData: Int
        get() = wrapped.integers.read(7)
        set(value) {
            wrapped.integers.write(7, value)
        }

    val meta: WrappedDataWatcher
        get() = wrapped.dataWatcherModifier.read(0)

    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.SPAWN_ENTITY_LIVING
    }
}

class WrappedPlayServerEntityVelocity(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }

    var velocityX: Double
        get() = wrapped.integers.read(1) / 8000.0
        set(value) {
            wrapped.integers.write(1, (value * 8000).toInt())
        }

    var velocityY: Double
        get() = wrapped.integers.read(2) / 8000.0
        set(value) {
            wrapped.integers.write(2, (value * 8000).toInt())
        }

    var velocityZ: Double
        get() = wrapped.integers.read(3) / 8000.0
        set(value) {
            wrapped.integers.write(3, (value * 8000).toInt())
        }

    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.ENTITY_VELOCITY
    }

}

class WrappedPlayServerEntitySpawn(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    var entityId: Int
        get() = wrapped.integers.read(0)
        set(value) {
            wrapped.integers.write(0, value)
        }

    var uniqueId: UUID
        get() = wrapped.uuiDs.read(0)
        set(value) {
            wrapped.uuiDs.write(0, value)
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

    var optionalXSpeed: Double
        get() = wrapped.integers.read(1) / 8000.0
        set(value) {
            wrapped.integers.write(1, (value * 8000).toInt())
        }

    var optionalYSpeed: Double
        get() = wrapped.integers.read(2) / 8000.0
        set(value) {
            wrapped.integers.write(2, (value * 8000).toInt())
        }
    var optionalZSpeed: Double
        get() = wrapped.integers.read(3) / 8000.0
        set(value) {
            wrapped.integers.write(3, (value * 8000).toInt())
        }

    var yaw: Float
        get() = (wrapped.integers.read(5) * 360) / 256.toFloat()
        set(value) {
            wrapped.integers.write(5, (value * 256 / 360).toInt())
        }

    var pitch: Float
        get() = (wrapped.integers.read(4) * 360) / 256.toFloat()
        set(value) {
            wrapped.integers.write(4, (value * 256 / 360).toInt())
        }

    var entityType: Int
        get() = wrapped.integers.read(6)
        set(value) {
            wrapped.integers.write(6, value)
        }

    var objectData: Int
        get() = wrapped.integers.read(7)
        set(value) {
            wrapped.integers.write(7, value)
        }

    fun getEntity(world: World) = wrapped.getEntityModifier(world).read(0)

    companion object {
        const val BOAT = 1
        const val ITEM_STACK = 2
        const val AREA_EFFECT_CLOUD = 3
        const val MINECART = 10
        const val ACTIVATED_TNT = 50
        const val ENDER_CRYSTAL = 51
        const val TIPPED_ARROW_PROJECTILE = 60
        const val SNOWBALL_PROJECTILE = 61
        const val EGG_PROJECTILE = 62
        const val GHAST_FIREBALL = 63
        const val BLAZE_FIREBALL = 64
        const val THROWN_ENDERPEARL = 65
        const val WITHER_SKULL_PROJECTILE = 66
        const val SHULKER_BULLET = 67
        const val FALLING_BLOCK = 70
        const val ITEM_FRAME = 71
        const val EYE_OF_ENDER = 72
        const val THROWN_POTION = 73
        const val THROWN_EXP_BOTTLE = 75
        const val FIREWORK_ROCKET = 76
        const val LEASH_KNOT = 77
        const val ARMORSTAND = 78
        const val FISHING_FLOAT = 90
        const val SPECTRAL_ARROW = 91
        const val DRAGON_FIREBALL = 93

        @JvmStatic
        val TYPE = PacketType.Play.Server.SPAWN_ENTITY
    }

}

class WrappedPlayServerEntityDestroy(container: PacketContainer) : AbstractPacketWrapper(container, TYPE) {

    constructor() : this(PacketContainer(TYPE)) {
        wrapped.modifier.writeDefaults()
    }

    val count = wrapped.integerArrays.read(0).size

    var entityIds
        get() = wrapped.integerArrays.read(0)
        set(value) {
            wrapped.integerArrays.write(0, value)
        }

    companion object {
        @JvmStatic
        val TYPE = PacketType.Play.Server.ENTITY_DESTROY
    }

}