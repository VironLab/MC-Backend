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

package eu.vironlab.mc.extension

import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.VextensionDownloader
import eu.vironlab.mc.feature.economy.DefaultEconomyFeature
import eu.vironlab.mc.language.DefaultLanguage
import eu.vironlab.mc.language.DefaultLanguageProvider
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.database.factory.createDatabaseClient
import eu.vironlab.vextension.database.mongo.MongoDatabaseClient
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.net.URI
import java.nio.file.Paths

fun CloudUtil.initOnService() {
    try {
        VextensionDownloader.loadVextension(
            File(
                URI(
                    CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("vextensionLibDir")
                        .getBlocking()
                        .getValue() ?: throw IllegalStateException("Cannot find Module")
                )
            )
        )
        this.dataFolder =
            Paths.get(
                URI(
                    CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("dataFolder").getBlocking()
                        .getValue()
                )
            )
        this.prefix =
            CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("prefix").getBlocking().getValue()
        this.dbClient = createDatabaseClient(MongoDatabaseClient::class.java) {
            this.connectionData =
                CloudAPI.instance.getGlobalPropertyHolder().requestProperty<ConfigDocument>("dbConnection")
                    .getBlocking()
                    .getValue().connectionData()
        }
        this.languageProvider = DefaultLanguageProvider().let { langProvider ->
            val langs =
                CloudAPI.instance.getGlobalPropertyHolder().requestProperty<MutableList<String>>("languages")
                    .getBlocking()
                    .getValue()
            langs.forEach { lang ->
                langProvider.registerLanguage(DefaultLanguage(lang, this.dbClient, this.prefix))
            }
            langProvider
        }
        this.economyProvider = DefaultEconomyFeature(
            CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("coinsPropertyName").getBlocking()
                .getValue()
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}