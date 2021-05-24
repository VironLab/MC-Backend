package eu.vironlab.mc.bukkit.replay.record

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import eu.vironlab.mc.bukkit.replay.ReplaySaver
import eu.vironlab.mc.bukkit.replay.data.*
import eu.vironlab.mc.bukkit.replay.packet.*
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item

class PacketRecorder(val replaySaver: ReplaySaver) {

    var recorded: MutableMap<String, MutableList<SerializedPacket>> = mutableMapOf()
    private val packetAdapter: PacketAdapter
    private val optimizer: PacketRecordOptimizer
    private val spawnedItemsCache: MutableList<Int> = mutableListOf()
    private val spawnedHooksCache: MutableList<Int> = mutableListOf()
    private val spawnedEntityCache: MutableMap<Int, SerializedEntity> = mutableMapOf()
    private val entityDict: MutableMap<Int, String> = mutableMapOf()
    private val idDict: MutableMap<Int, Entity> = mutableMapOf()

    init {
        this.optimizer = PacketRecordOptimizer()
        this.packetAdapter = RecorderPacketAdapter()
        ProtocolLibrary.getProtocolManager().asynchronousManager.registerAsyncHandler(packetAdapter).start()
    }

    fun append(name: String, data: SerializedPacket) {
        if (optimizer.canSkip(data)) {
            return
        }
        this.recorded[name]?.add(data) ?: mutableListOf(data).also { this.recorded[name] = it }
    }

    inner class RecorderPacketAdapter() : PacketAdapter(
        replaySaver.bukkitLoader,
        ListenerPriority.HIGHEST,
        PacketType.Play.Client.POSITION,
        PacketType.Play.Client.POSITION_LOOK,
        PacketType.Play.Client.LOOK,
        PacketType.Play.Client.ENTITY_ACTION,
        PacketType.Play.Client.ARM_ANIMATION,
        PacketType.Play.Client.BLOCK_DIG,
        PacketType.Play.Server.SPAWN_ENTITY,
        PacketType.Play.Server.ENTITY_DESTROY,
        PacketType.Play.Server.ENTITY_VELOCITY,
        PacketType.Play.Server.SPAWN_ENTITY_LIVING,
        PacketType.Play.Server.REL_ENTITY_MOVE,
        PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
        PacketType.Play.Server.ENTITY_LOOK,
        PacketType.Play.Server.POSITION,
        PacketType.Play.Server.ENTITY_TELEPORT
    ) {

        override fun onPacketReceiving(event: PacketEvent) {
            val player = event.player
            when (event.packetType) {
                PacketType.Play.Client.POSITION -> {
                    WrappedPlayClientPosition(event.packet).also { packet ->
                        append(
                            player.name,
                            SerializedLocation(
                                packet.x,
                                packet.y,
                                packet.z,
                                player.location.pitch,
                                player.location.yaw
                            )
                        )
                    }
                }
                PacketType.Play.Client.LOOK -> {
                    WrappedPlayClientLook(event.packet).also { packet ->
                        append(
                            player.name,
                            SerializedLocation(
                                player.location.x,
                                player.location.y,
                                player.location.z,
                                packet.pitch,
                                packet.yaw
                            )
                        )
                    }
                }
                PacketType.Play.Client.POSITION_LOOK -> {
                    WrappedPlayClientPositionLook(event.packet).also { packet ->
                        append(
                            player.name,
                            SerializedLocation(
                                packet.x,
                                packet.y,
                                packet.z,
                                packet.pitch,
                                packet.yaw
                            )
                        )
                    }
                }
                PacketType.Play.Client.ENTITY_ACTION -> {
                    WrappedPlayClientEntityAction(event.packet).also { packet ->
                        if (EnumWrappers.PlayerAction.START_SNEAKING.equals(packet.action) || EnumWrappers.PlayerAction.STOP_SNEAKING.equals(
                                packet.action
                            )
                        ) {
                            append(player.name, SerializedEntityAction(packet.action))
                        }
                    }
                }
                PacketType.Play.Client.ARM_ANIMATION -> {
                    append(player.name, SerializedAnimation(0))
                }
                PacketType.Play.Client.BLOCK_DIG -> {
                    WrappedPlayClientBlockDig(event.packet).also {
                        val state = replaySaver.recorded.getState(player.uniqueId)
                            ?: throw IllegalStateException("Invalid Player")
                        if (state.blocking) {
                            state.blocking = false
                            append(
                                player.name,
                                SerializedMetaData(state.burning, false, state.gliding)
                            )
                        }
                    }
                }
            }
        }

        override fun onPacketSending(event: PacketEvent) {
            val player = event.player
            when (event.packetType) {
                PacketType.Play.Server.SPAWN_ENTITY -> {
                    WrappedPlayServerEntitySpawn(event.packet).also { packet ->
                        var location = SerializedWorldLocation(
                            packet.x,
                            packet.y,
                            packet.z,
                            world = player.world.name
                        )
                        if (EntityType.DROPPED_ITEM.equals(event.packet.entityTypeModifier.read(0)) && spawnedItemsCache.contains(
                                packet.entityId
                            )
                        ) {
                            val entity = packet.getEntity(player.world) ?: return
                            if (entity !is Item) {
                                return
                            }
                            val velocity =
                                entity.velocity.let { vector -> SerializedVector(vector.x, vector.y, vector.z) }
                            append(
                                player.name, SerializedEntityItem(
                                    0,
                                    packet.entityId,
                                    SerializedItemStack.fromItemStack(entity.itemStack),
                                    location, velocity
                                )
                            )
                            spawnedItemsCache.add(packet.entityId)
                        }
                        if (EntityType.FISHING_HOOK.equals(event.packet.entityTypeModifier.read(0)) && spawnedHooksCache.contains(
                                packet.entityId
                            )
                        ) {
                            location = SerializedWorldLocationLook.fromWorldLoc(location, packet.pitch, packet.yaw)
                            append(
                                player.name, SerializedFishingData(
                                    packet.entityId,
                                    location,
                                    packet.optionalXSpeed,
                                    packet.optionalYSpeed,
                                    packet.optionalZSpeed
                                )
                            )
                            spawnedHooksCache.add(packet.entityId)
                        }
                    }
                }
                PacketType.Play.Server.SPAWN_ENTITY_LIVING -> {
                    WrappedPlayServerSpawnEntityLiving(event.packet).also { packet ->
                        val type = EntityType.fromId(packet.entityId) ?: packet.getEntity(player.world).type
                        if (spawnedEntityCache.containsKey(packet.entityId)) {
                            return
                        }
                        val location = SerializedWorldLocation(packet.x, packet.y, packet.z, world = player.world.name)
                        val entity = SerializedEntity(packet.entityId, 0, location, type.toString())
                        append(player.name, entity)
                        spawnedEntityCache[packet.entityId] = entity
                        entityDict[packet.entityId] = player.name
                        idDict[packet.entityId] = packet.getEntity(player.world)!!
                    }
                }
                PacketType.Play.Server.ENTITY_DESTROY -> {
                    WrappedPlayServerEntityDestroy(event.packet).also { packet ->
                        for (id in packet.entityIds) {
                            if (spawnedItemsCache.contains(id)) {
                                append(player.name, SerializedEntityItem(1, id))
                                spawnedItemsCache -= id
                            }
                            if (spawnedEntityCache.containsKey(id) && idDict[id] == null || (idDict[id] != null && idDict[id]!!.isDead)) {
                                val entity = spawnedEntityCache[id]!!
                                append(player.name, SerializedEntity(id, 1, entity.location, entity.type))
                                spawnedEntityCache -= id
                                idDict -= id
                                entityDict -= id
                            }
                            if (spawnedHooksCache.contains(id)) {
                                append(player.name, SerializedEntityItem(2, id))
                                spawnedHooksCache -= id
                            }
                        }
                    }
                }
                PacketType.Play.Server.ENTITY_VELOCITY -> {
                    WrappedPlayServerEntityVelocity(event.packet).also { packet ->
                        if (spawnedHooksCache.contains(packet.entityId) || (entityDict.containsKey(packet.entityId) && entityDict[packet.entityId].equals(
                                player.name,
                                true
                            ))
                        ) {
                            append(
                                player.name,
                                SerializedVelocity(
                                    packet.entityId,
                                    packet.velocityX,
                                    packet.velocityY,
                                    packet.velocityZ
                                )
                            )
                        }
                    }
                }
                PacketType.Play.Server.REL_ENTITY_MOVE -> {
                    WrappedPlayServerRelEntityMove(event.packet).also { packet ->
                        if (entityDict.containsKey(packet.entityId) && entityDict[packet.entityId].equals(
                                player.name,
                                true
                            )
                        ) {
                            val location = packet.getEntity(player.world)?.location ?: return
                            append(
                                player.name,
                                SerializedEntityLocation(
                                    packet.entityId,
                                    location.x,
                                    location.y,
                                    location.z,
                                    location.pitch,
                                    location.yaw
                                )
                            )
                        }
                    }
                }
                PacketType.Play.Server.REL_ENTITY_MOVE_LOOK -> {
                    WrappedPlayServerRelEntityMoveLook(event.packet).also { packet ->
                        if (entityDict.containsKey(packet.entityId) && entityDict[packet.entityId].equals(
                                player.name,
                                true
                            )
                        ) {
                            val loc = packet.getEntity(player.world)?.location ?: return
                            append(
                                player.name,
                                SerializedEntityLocation(packet.entityId, loc.x, loc.y, loc.z, loc.pitch, loc.yaw)
                            )
                        }
                    }
                }
                PacketType.Play.Server.ENTITY_TELEPORT -> {
                    WrappedPlayServerEntityTeleport(event.packet).also { packet ->
                        if (entityDict.containsKey(packet.entityId) && entityDict[packet.entityId].equals(
                                player.name,
                                true
                            )
                        ) {
                            val loc = packet.getEntity(player.world)?.location ?: return
                            append(
                                player.name,
                                SerializedEntityLocation(packet.entityId, loc.x, loc.y, loc.z, packet.pitch, packet.yaw)
                            )
                        }
                    }
                }
            }
        }
    }

    inner class PacketRecordOptimizer {
        fun canSkip(packet: SerializedPacket): Boolean {
            //TODO: Validate Packets and choose if skip is possible
            return false
        }
    }

}