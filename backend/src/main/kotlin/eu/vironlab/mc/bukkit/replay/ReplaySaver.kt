package eu.vironlab.mc.bukkit.replay

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.PacketTypeEnum
import com.comphenix.protocol.ProtocolLib
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.*
import com.comphenix.protocol.injector.packet.PacketRegistry
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import eu.thesimplecloud.plugin.startup.CloudPlugin
import eu.vironlab.mc.bukkit.BukkitLoader
import eu.vironlab.mc.feature.moderation.SerializedReplay
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.BufferedInputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Files
import java.util.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin

class ReplaySaver(val bukkitLoader: BukkitLoader) : Listener {

    val tempFile: File = File(bukkitLoader.dataFolder.also { it.mkdirs() }, "temp.replay").also { it.createNewFile() }
    val replayWriter = FileWriter(tempFile)
    val protocolManager = ProtocolLibrary.getProtocolManager().asynchronousManager.also { manager ->
        manager.registerAsyncHandler(object : PacketAdapter(
            bukkitLoader,
            ListenerPriority.LOW,

            //Player Login
            PacketType.Login.Client.START,
            PacketType.Login.Server.SUCCESS,
            PacketType.Login.Server.DISCONNECT,
            PacketType.Login.Server.SET_COMPRESSION,

            //Play - Client
            PacketType.Play.Client.KEEP_ALIVE,
            PacketType.Play.Client.CHAT,
            PacketType.Play.Client.POSITION,
            PacketType.Play.Client.POSITION_LOOK,
            PacketType.Play.Client.BLOCK_PLACE,
            PacketType.Play.Client.WINDOW_CLICK,
            PacketType.Play.Client.CLOSE_WINDOW,
            PacketType.Play.Client.TRANSACTION,
            PacketType.Play.Client.USE_ENTITY,
            PacketType.Play.Client.ENTITY_ACTION,
            PacketType.Play.Client.ARM_ANIMATION,
            PacketType.Play.Client.TAB_COMPLETE,
            PacketType.Play.Client.HELD_ITEM_SLOT,

            //Play - Server
            PacketType.Play.Server.KEEP_ALIVE,
            PacketType.Play.Server.KICK_DISCONNECT,
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.CHAT,
            PacketType.Play.Server.ABILITIES,
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.ENTITY_LOOK,
            PacketType.Play.Server.ENTITY_HEAD_ROTATION,
            PacketType.Play.Server.SPAWN_ENTITY,
            PacketType.Play.Server.ENTITY_METADATA,
            PacketType.Play.Server.ENTITY_DESTROY,
            PacketType.Play.Server.ENTITY_VELOCITY,
            PacketType.Play.Server.BLOCK_CHANGE,
            PacketType.Play.Server.MULTI_BLOCK_CHANGE,
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.RESPAWN,
            PacketType.Play.Server.EXPLOSION,
            PacketType.Play.Server.MAP_CHUNK_BULK,
            PacketType.Play.Server.ENTITY_EQUIPMENT,
            PacketType.Play.Server.SET_SLOT,
            PacketType.Play.Server.WINDOW_ITEMS,
            PacketType.Play.Server.OPEN_WINDOW,
            PacketType.Play.Server.CLOSE_WINDOW,
            PacketType.Play.Server.TRANSACTION,
            PacketType.Play.Server.GAME_STATE_CHANGE,
            PacketType.Play.Server.SCOREBOARD_SCORE,
            PacketType.Play.Server.SCOREBOARD_TEAM,
            PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
            PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE,
            PacketType.Play.Server.CAMERA,
            PacketType.Play.Server.ANIMATION,
            PacketType.Play.Server.BLOCK_ACTION,
            PacketType.Play.Server.TAB_COMPLETE,
            PacketType.Play.Server.EXPERIENCE,
            PacketType.Play.Server.HELD_ITEM_SLOT,
        ) {
            override fun onPacketSending(event: PacketEvent) {
                event.packet.toString()
                replayWriter.write("${Bukkit.getServer().currentTick};${Gson().toJson(event.packet)}|||")
            }
        }).start()
    }
    val serviceId = CloudPlugin.instance.thisService().getUniqueId()
    val players: MutableList<UUID> = mutableListOf()

    @EventHandler
    fun handleJoin(e: PlayerJoinEvent) {
        this.players.add(e.player.uniqueId)
    }

    fun stop() {
        if (saveReplay) {
            val saveDir: File = File(
                bukkitLoader.backendDataFolder,
                "/replays/${serviceId}"
            ).also {
                Files.createDirectories(it.toPath())
            }
            replayWriter.flush()
            replayWriter.close()
            Files.copy(tempFile.toPath(), File(saveDir, "save.replay").toPath())
            val data = ConfigDocument(
                File(saveDir, "data.json"),
                document(SerializedReplay(serviceId, players, CloudPlugin.instance.thisService().getGroupName()))
            ).also { it.saveConfig() }

        }
    }

    companion object {
        @JvmStatic
        var saveReplay: Boolean = false

    }

}