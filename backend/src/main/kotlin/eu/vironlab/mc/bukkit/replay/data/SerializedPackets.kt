package eu.vironlab.mc.bukkit.replay.data

import com.comphenix.protocol.wrappers.EnumWrappers
import org.bukkit.Color
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta

abstract class SerializedPacket {
}

data class SerializedLocation(val x: Double, val y: Double, val z: Double, val pitch: Float, val yaw: Float) :
    SerializedPacket()

data class SerializedEntityLocation(
    val id: Int,
    var x: Double,
    var y: Double,
    var z: Double,
    var pitch: Float,
    var yaw: Float,
): SerializedPacket()

class SerializedWorldLocationLook(
    x0: Double,
    y0: Double,
    z0: Double,
    var pitch: Float,
    var yaw: Float,
    world0: String
) : SerializedWorldLocation(x0, y0, z0, world0) {
    companion object {
        @JvmStatic
        fun fromWorldLoc(location: SerializedWorldLocation, pitch: Float, yaw: Float) = SerializedWorldLocationLook(
            location.x,
            location.y,
            location.z,
            pitch,
            yaw,
            location.world
        )
    }
}

open class SerializedWorldLocation(
    open var x: Double,
    open var y: Double,
    open var z: Double,
    open var world: String
): SerializedPacket()


data class SerializedVector(
    val x: Double,
    val y: Double,
    val z: Double,
) : SerializedPacket()

data class SerializedVelocity(val id: Int, val x: Double, val y: Double, val z: Double) : SerializedPacket()

data class SerializedEntityAction(val action: EnumWrappers.PlayerAction) : SerializedPacket()

data class SerializedAnimation(val id: Int) : SerializedPacket()

data class SerializedMetaData(val burning: Boolean, val blocking: Boolean, val gliding: Boolean = false) :
    SerializedPacket()

data class SerializedFishingData(
    val id: Int,
    val location: SerializedWorldLocation,
    val x: Double,
    val y: Double,
    val z: Double
) : SerializedPacket()

data class SerializedEntity(val id: Int, val action: Int, val location: SerializedWorldLocation, val type: String) :
    SerializedPacket()

data class SerializedItemStack(
    var itemStack: MutableMap<String, Any>,
    var hasEnchantment: Boolean = false,
    var color: Int = 0,
    var hasColor: Boolean = false
) {
    fun toItemStack(): ItemStack {
        return ItemStack.deserialize(this.itemStack).also { item ->
            if (this.hasEnchantment) {
                item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            }
            if (this.hasColor) {
                item.setItemMeta((item.itemMeta as LeatherArmorMeta).also { meta ->
                    meta.setColor(Color.fromBGR(this.color))
                })
            }
        }
    }

    companion object {
        @JvmStatic
        fun fromItemStack(item: ItemStack, block: Boolean = false): SerializedItemStack {
            val itemStack = SerializedItemStack(
                item.serialize().let {
                    it.entries.removeIf { e ->
                        !e.key.equals("v", true) && !e.key.equals("type", true) && !e.key
                            .equals("damage", true)
                    }
                    it
                }
            )
            if (!block) {
                itemStack.hasEnchantment = item.enchantments.isNotEmpty()
                if (item.hasItemMeta() && item.itemMeta is LeatherArmorMeta) {
                    itemStack.hasColor = true
                    itemStack.color = (item.itemMeta as LeatherArmorMeta).color.asRGB()
                }
            }
            return itemStack
        }
    }
}

data class SerializedEntityItem(
    val action: Int,
    val id: Int,
    val item: SerializedItemStack? = null,
    val location: SerializedWorldLocation? = null,
    val velocity: SerializedVector? = null
) : SerializedPacket()