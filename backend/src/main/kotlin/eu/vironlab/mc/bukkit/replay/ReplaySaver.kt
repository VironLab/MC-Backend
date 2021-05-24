package eu.vironlab.mc.bukkit.replay

import eu.thesimplecloud.plugin.startup.CloudPlugin
import eu.vironlab.mc.bukkit.BukkitLoader
import eu.vironlab.mc.bukkit.replay.data.Action
import eu.vironlab.mc.bukkit.replay.data.ActionType
import eu.vironlab.mc.bukkit.replay.data.RecordedReplay
import eu.vironlab.mc.bukkit.replay.record.PacketRecorder
import eu.vironlab.mc.bukkit.replay.world.WorldChecker
import eu.vironlab.mc.feature.moderation.SerializedReplay
import eu.vironlab.mc.feature.moderation.packet.replay.PacketSaveReplay
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.nio.file.Files
import java.util.*
import java.util.zip.GZIPOutputStream
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.scheduler.BukkitRunnable

class ReplaySaver(val bukkitLoader: BukkitLoader) : Listener {

    var currentTick: Long = 0
    val runnable: BukkitRunnable
    val recorded: RecordedReplay = RecordedReplay(mutableMapOf(), mutableMapOf())
    val serviceId = CloudPlugin.instance.thisService().getUniqueId()
    val players: MutableList<UUID> = mutableListOf()
    val recorder: PacketRecorder = PacketRecorder(this)
    val worldChecker: WorldChecker

    init {
        CloudPlugin.instance.communicationClient.getPacketManager().registerPacket(PacketSaveReplay::class.java)
        this.worldChecker = WorldChecker(
            File(
                bukkitLoader.backendDataFolder,
                "/replay/worlds/${CloudPlugin.instance.thisService().getGroupName()}/"
            ).also {
                if (!it.exists()) {
                    Files.createDirectories(it.toPath())
                }
            })
        this.runnable = object : BukkitRunnable() {
            override fun run() {
                val tmp = recorder.recorded
                for (key in tmp.keys) {
                    val packets = tmp[key]!!.iterator()
                    while (packets.hasNext()) {
                        val packet = packets.next()
                        append(currentTick, Action(currentTick, ActionType.PACKET, packet, key))
                    }
                }
                recorder.recorded.clear()
                currentTick++
            }
        }
    }

    fun append(tick: Long, action: Action) =
        this.recorded.actions[tick]?.add(action)
            ?: mutableListOf(action).also { list -> this.recorded.actions[tick] = list }

    @EventHandler
    fun handleJoin(e: PlayerJoinEvent) {
        this.players.add(e.player.uniqueId)
    }

    fun start() {
        this.runnable.runTaskTimerAsynchronously(bukkitLoader, 1L, 1L)
    }

    fun stop() {
        if (saveReplay) {
            val saveDir: File = File(
                bukkitLoader.backendDataFolder,
                "/replays/${serviceId}"
            ).also {
                Files.createDirectories(it.toPath())
            }
            val file = File(saveDir, "save.replay").also { it.createNewFile() }
            FileOutputStream(file).also { fileOut ->
                GZIPOutputStream(fileOut).also { gzipOut ->
                    ObjectOutputStream(gzipOut).also { objOut ->
                        objOut.writeObject(this.recorded.let { it.states.clear(); it })
                        objOut.flush()
                        objOut.close()
                    }
                }.close()
            }.close()
            ConfigDocument(
                File(saveDir, "data.json"),
                document(
                    SerializedReplay(
                        serviceId,
                        players,
                        CloudPlugin.instance.thisService().getGroupName(),
                        currentTick,
                        System.currentTimeMillis(), mutableListOf()
                    )
                )
            ).also { it.saveConfig() }

        }
    }

    companion object {
        @JvmStatic
        var saveReplay: Boolean = false

    }

}