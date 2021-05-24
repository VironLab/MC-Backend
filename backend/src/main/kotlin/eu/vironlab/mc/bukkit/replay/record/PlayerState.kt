package eu.vironlab.mc.bukkit.replay.record

import com.comphenix.protocol.wrappers.WrappedDataWatcher
import eu.vironlab.vextension.lang.Nameable

data class PlayerState(
    override val name: String,
    var sneaking: Boolean = false,
    var burning: Boolean = false,
    var blocking: Boolean = false,
    var gliding: Boolean = false,
    var swimming: Boolean = false
) : Nameable {
    fun isOneActive(): Boolean = sneaking || burning || blocking || gliding || swimming
}