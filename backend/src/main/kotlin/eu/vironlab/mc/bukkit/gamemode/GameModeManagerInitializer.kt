package eu.vironlab.mc.bukkit.gamemode

import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.Backend
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.nio.file.Files

class GameModeManagerInitializer(val backend: Backend) {

    init {
        val messages = ConfigDocument(File(File(backend.dataFolder, "gamemode").also {
            if (!it.exists()) {
                Files.createDirectory(it.toPath())
            }
        }, "messages.json"))
        messages.get("messages", GameModeMessageConfiguration::class.java, GameModeMessageConfiguration())
        messages.saveConfig()
        CloudAPI.instance.getGlobalPropertyHolder().setProperty<GameModeMessageConfiguration>("gamemodeConfig", messages.get("messages", GameModeMessageConfiguration::class.java)!!)
    }

}
