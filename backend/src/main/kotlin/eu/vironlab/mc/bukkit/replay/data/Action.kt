package eu.vironlab.mc.bukkit.replay.data

import eu.vironlab.vextension.lang.Nameable

data class Action(
    val index: Long,
    val type: ActionType,
    val packetData: SerializedPacket,
    override val name: String,
) : Nameable

enum class ActionType {
    PACKET, SPAWN, DESPAWN, DEATH, WORLD, MESSAGE, CUSTOM
}