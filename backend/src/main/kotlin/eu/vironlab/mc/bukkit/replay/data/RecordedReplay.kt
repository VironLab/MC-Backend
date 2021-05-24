package eu.vironlab.mc.bukkit.replay.data

import eu.vironlab.mc.bukkit.replay.record.PlayerState
import java.util.*

data class RecordedReplay(var actions: MutableMap<Long, MutableList<Action>>, var states: MutableMap<UUID, PlayerState>) {
    fun getState(uuid: UUID): PlayerState? = states[uuid]
}