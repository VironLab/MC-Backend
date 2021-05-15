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
import eu.vironlab.mc.feature.economy.DefaultEconomyFeature
import eu.vironlab.mc.extension.connectionData
import eu.vironlab.mc.feature.DefaultFeatureRegistry
import eu.vironlab.mc.feature.broadcast.DefaultBroadcastFeature
import eu.vironlab.mc.feature.punishment.DefaultPunishmentFeature
import eu.vironlab.mc.language.DefaultLanguage
import eu.vironlab.mc.language.DefaultLanguageProvider
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.collection.DataPair
import eu.vironlab.vextension.database.DatabaseClient
import eu.vironlab.vextension.database.factory.createDatabaseClient
import eu.vironlab.vextension.database.mongo.MongoDatabaseClient
import eu.vironlab.vextension.document.Document
import eu.vironlab.vextension.document.DocumentFactory
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.nio.file.Files

class Backend : ICloudModule {

    lateinit var dataFolder: File

    override fun onDisable() {
    }

    override fun isReloadable(): Boolean {
        return false
    }

    override fun onEnable() {
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
            CloudUtil.init(
                db.first,
                config.getString("prefix")!!,
                dataFolder.toPath(),
                DefaultLanguageProvider().let {
                    it.registerLanguage(DefaultLanguage("english", db.first, config.getString("prefix")!!))
                    it.registerLanguage(DefaultLanguage("german", db.first, config.getString("prefix")!!))
                    it
                },
                DefaultFeatureRegistry()
            )
            CloudAPI.instance.getGlobalPropertyHolder().let {
                it.setProperty<String>("dataFolder", dataFolder.toPath().toUri().toString())
                it.setProperty<String>("prefix", CloudUtil.prefix)
                it.setProperty<MutableList<String>>(
                    "languages",
                    CloudUtil.languageProvider.languages.keys.toMutableList()
                )
                it.setProperty<String>("coinsPropertyName", "coins")
                it.setProperty<ConfigDocument>("dbConnection", db.second)
            }
            startFeatures(config.getDocument("features")!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startFeatures(cfg: Document) {
        val featureDir = File(dataFolder, "features").let {
            if (!it.exists()) {
                Files.createDirectories(it.toPath())
            }
            it
        }
        cfg.getKeys().forEach {
            val cfgDir = File(featureDir, it)
            if (!cfgDir.exists()) {
                Files.createDirectories(cfgDir.toPath())
            }
        }
        if (cfg.getBoolean("broadcast")!!) {
            DefaultBroadcastFeature(File(dataFolder, "feature/broadcast/autobroadcast.json"))
        }
        if (cfg.getBoolean("punishment")!!) {
            DefaultPunishmentFeature(CloudUtil, File(featureDir, "punishment/"))
        }
    }

    fun initConfig(): ConfigDocument {
        val config = ConfigDocument(File(dataFolder, "config.json"))
        config.loadConfig()

        //Features
        val features = document()
        features.getBoolean("broadcast", true)
        features.getBoolean("punishment", true)

        config.let {
            it.getString("prefix", "§2§lViron§a§lLab §8| §7")
            it.getDocument("features", features)
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