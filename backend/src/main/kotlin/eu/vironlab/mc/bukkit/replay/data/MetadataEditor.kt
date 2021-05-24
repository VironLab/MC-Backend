package eu.vironlab.mc.bukkit.replay.data

import com.comphenix.protocol.wrappers.WrappedDataWatcher
import eu.vironlab.vextension.util.ServerUtil
import java.lang.reflect.Method
import org.bukkit.entity.Entity

class MetadataEditor(val watcher: WrappedDataWatcher) {

    constructor(entity: Entity) : this(WrappedDataWatcher.getEntityWatcher(entity).deepClone())

    fun write(index: Int, value: Any): MetadataEditor = watcher.setObject(index, value).let { this }

    fun setInvisible() = write(0, 0x20.toByte())
    fun setCrouched() = write(0, 0x02.toByte())
    fun setArrows(amount: Int) = write(11, amount)
    fun setGlowing() = write(0, 0x20.toByte())
    fun setSilent() = write(4, true)
    fun setNoGravity() = write(5, true)
    fun setHealth(health: Float) = write(7, health)
    fun setAir(amount: Int) = write(1, amount)
    fun reset() = write(0, 0.toByte()).setPoseField("STANDING")

    private fun setPoseField(type: String): MetadataEditor =
        ENTITY_VALUE_METHOD.invoke(null, type).let { write(6, it); this }

    companion object {
        @JvmStatic
        val ENTITY_POSE_CLASS: Class<*> = Class.forName("${ServerUtil.NMS_PACKAGE_NAME}.EntityPose")

        @JvmStatic
        val ENTITY_VALUE_METHOD: Method = ENTITY_POSE_CLASS.getMethod("valueOf", String::class.java)
    }

}
