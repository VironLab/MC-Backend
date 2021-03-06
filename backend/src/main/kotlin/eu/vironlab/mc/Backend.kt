/**
 *   Copyright © 2020 | vironlab.eu | All Rights Reserved.<p>
 * <p>
 *      ___    _______                        ______         ______  <p>
 *      __ |  / /___(_)______________ _______ ___  / ______ ____  /_ <p>
 *      __ | / / __  / __  ___/_  __ \__  __ \__  /  _  __ `/__  __ \<p>
 *      __ |/ /  _  /  _  /    / /_/ /_  / / /_  /___/ /_/ / _  /_/ /<p>
 *      _____/   /_/   /_/     \____/ /_/ /_/ /_____/\__,_/  /_.___/ <p>
 *<p>
 *    ____  _______     _______ _     ___  ____  __  __ _____ _   _ _____ <p>
 *   |  _ \| ____\ \   / / ____| |   / _ \|  _ \|  \/  | ____| \ | |_   _|<p>
 *   | | | |  _|  \ \ / /|  _| | |  | | | | |_) | |\/| |  _| |  \| | | |  <p>
 *   | |_| | |___  \ V / | |___| |__| |_| |  __/| |  | | |___| |\  | | |  <p>
 *   |____/|_____|  \_/  |_____|_____\___/|_|   |_|  |_|_____|_| \_| |_|  <p>
 *<p>
 *<p>
 *   This program is free software: you can redistribute it and/or modify<p>
 *   it under the terms of the GNU General Public License as published by<p>
 *   the Free Software Foundation, either version 3 of the License, or<p>
 *   (at your option) any later version.<p>
 *<p>
 *   This program is distributed in the hope that it will be useful,<p>
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of<p>
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<p>
 *   GNU General Public License for more details.<p>
 *<p>
 *   You should have received a copy of the GNU General Public License<p>
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.<p>
 *<p>
 *   Contact:<p>
 *<p>
 *     Discordserver:   https://discord.gg/wvcX92VyEH<p>
 *     Website:         https://vironlab.eu/ <p>
 *     Mail:            contact@vironlab.eu<p>
 *<p>
 */

package eu.vironlab.mc

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.external.ICloudModule
import eu.thesimplecloud.launcher.startup.Launcher
import eu.vironlab.mc.bukkit.gamemode.GameModeManagerInitializer
import eu.vironlab.mc.bukkit.menu.manager.PlayerMenuManagerInitializer
import eu.vironlab.mc.config.BackendMessageConfiguration
import eu.vironlab.mc.extension.connectionData
import eu.vironlab.mc.feature.BackendFeatureConfiguration
import eu.vironlab.mc.feature.DefaultFeatureRegistry
import eu.vironlab.mc.feature.broadcast.ManagerBroadcastFeature
import eu.vironlab.mc.feature.broadcast.command.BroadcastCommand
import eu.vironlab.mc.feature.broadcast.manager.DefaultManagerBroadcastFeature
import eu.vironlab.mc.feature.economy.EconomyCommand
import eu.vironlab.mc.feature.economy.manager.DefaultManagerEconomyFeature
import eu.vironlab.mc.feature.help.HelpCommand
import eu.vironlab.mc.feature.help.HelpFeature
import eu.vironlab.mc.feature.moderation.PunishmentCommand
import eu.vironlab.mc.feature.moderation.command.UnpunishCommand
import eu.vironlab.mc.feature.moderation.manager.DefaultManagerModerationFeature
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.mc.util.EventUtil
import eu.vironlab.mc.util.ManagerGlobalEventProvider
import eu.vironlab.vextension.collection.DataPair
import eu.vironlab.vextension.database.DatabaseClient
import eu.vironlab.vextension.database.factory.createDatabaseClient
import eu.vironlab.vextension.database.mongo.MongoDatabaseClient
import eu.vironlab.vextension.document.DocumentFactory
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.nio.file.Files

class Backend : ICloudModule {

    lateinit var dataFolder: File
    lateinit var config: ConfigDocument
    lateinit var messages: BackendMessageConfiguration
    lateinit var featureRegistry: DefaultFeatureRegistry

    companion object {
        @JvmStatic
        lateinit var instance: Backend
    }

    override fun onDisable() {
    }

    override fun isReloadable(): Boolean {
        return false
    }

    override fun onEnable() {
        instance = this
        try {
            VextensionDownloader.loadVextension(File(".libs").let {
                if (!it.exists()) {
                    Files.createDirectories(it.toPath())
                }
                CloudAPI.instance.getGlobalPropertyHolder()
                    .setProperty<String>("vextensionLibDir", it.toURI().toString())
                it
            })
            this.dataFolder = File(
                DocumentFactory.documentJsonStorage.read(File("launcher.json")).getDocument("directoryPaths")
                    ?.getString("modulesPath") ?: throw IllegalStateException("The launcher.json is invalid"),
                "/backend/"
            )
            this.dataFolder.mkdirs()

            // Init Main Config
            val config: ConfigDocument = initConfig()
            val db = initDatabase()
            this.featureRegistry = DefaultFeatureRegistry()
            CloudUtil.init(
                db.first,
                config.getString("prefix")!!,
                dataFolder.toPath(),
                this.featureRegistry,
            )
            startFeatures(config.get("features", BackendFeatureConfiguration::class.java)!!)
            initBukkit(config.get("features", BackendFeatureConfiguration::class.java)!!)
            EventUtil.instance = ManagerGlobalEventProvider()
            CloudAPI.instance.getGlobalPropertyHolder().let {
                it.setProperty<BackendMessageConfiguration>("backendMessageConfig", this.messages)
                it.setProperty<String>("dataFolder", dataFolder.toPath().toUri().toString())
                it.setProperty<String>("prefix", CloudUtil.prefix)
                it.setProperty<String>("coinsPropertyName", "coins")
                it.setProperty<String>("dbConnection", db.second.toJson())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startFeatures(cfg: BackendFeatureConfiguration) {
        CloudAPI.instance.getGlobalPropertyHolder().setProperty("features", cfg)
        cfg.javaClass.declaredFields.forEach {
            val cfgDir = File(dataFolder, it.name)
            if (!cfgDir.exists()) {
                Files.createDirectories(cfgDir.toPath())
            }
        }
        if (cfg.broadcast) {
            val broadcast = DefaultManagerBroadcastFeature(File(dataFolder, "/broadcast/autobroadcast.json"))
            Launcher.instance.commandManager.registerCommand(
                this,
                BroadcastCommand(broadcast)
            )
            this.featureRegistry.registerFeature(ManagerBroadcastFeature::class.java, broadcast)
        }
        if (cfg.moderation) {
            val punish = DefaultManagerModerationFeature(CloudUtil, File(dataFolder, "moderation/"))
            Launcher.instance.commandManager.registerCommand(this, PunishmentCommand(punish, punish.messages))
            Launcher.instance.commandManager.registerCommand(this, UnpunishCommand(punish, punish.messages))
        }
        if (cfg.help) {
            val feature = HelpFeature(this)
            Launcher.instance.commandManager.registerCommand(this, HelpCommand(feature))
        }
        if (cfg.economy) {
            val eco = DefaultManagerEconomyFeature("coins", File(dataFolder, "economy/"))
            Launcher.instance.commandManager.registerCommand(this, EconomyCommand(eco, eco.messages))
        }
    }

    fun initBukkit(cfg: BackendFeatureConfiguration) {
        if (cfg.playermenu) {
            PlayerMenuManagerInitializer(this)
        }
        if (cfg.gamemode) {
            GameModeManagerInitializer(this)
        }
        CloudAPI.instance.getGlobalPropertyHolder().setProperty("bukkitConfig", cfg)
    }

    fun initConfig(): ConfigDocument {
        val msgConfig = ConfigDocument(File(dataFolder, "messages.json")).also {
            it.loadConfig()
        }
        this.messages =
            msgConfig.get("messages", BackendMessageConfiguration::class.java, BackendMessageConfiguration())
        msgConfig.saveConfig()
        this.config = ConfigDocument(File(dataFolder, "config.json"))
        config.loadConfig()

        config.let {
            it.getString("prefix", "§2§lViron§a§lLab §8| §7")
            it.get("features", BackendFeatureConfiguration::class.java, BackendFeatureConfiguration())
        }
        config.saveConfig()
        return config
    }

    fun initDatabase(): DataPair<DatabaseClient, ConfigDocument> {
        val dbConfig = ConfigDocument(File("modules/backend/database.json"))
        dbConfig.loadConfig()
        dbConfig.getString("host", "localhost")
        dbConfig.getInt("port", 27017)
        dbConfig.getString("database", "cloud")
        dbConfig.getString("username", "cloud")
        dbConfig.getString("password", "cloud")
        dbConfig.saveConfig()
        val dbClient = createDatabaseClient(MongoDatabaseClient::class.java) {
            this.connectionData = dbConfig.connectionData()
        }
        dbClient.init()
        return DataPair(dbClient, dbConfig)
    }

}