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
import eu.vironlab.mc.feature.BackendFeatureConfiguration
import eu.vironlab.mc.feature.DefaultFeatureRegistry
import eu.vironlab.mc.feature.broadcast.service.DefaultServiceBroadcastFeature
import eu.vironlab.mc.feature.economy.ServiceEconomyFeature
import eu.vironlab.mc.feature.economy.service.DefaultServiceEconomyFeature
import eu.vironlab.mc.feature.moderation.ServiceModerationFeature
import eu.vironlab.mc.feature.moderation.service.DefaultServiceModerationFeature
import eu.vironlab.mc.service.feature.broadcast.ServiceBroadcastFeature
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.mc.util.EventUtil
import eu.vironlab.mc.util.ServiceGlobalEventProvider
import eu.vironlab.vextension.database.factory.createDatabaseClient
import eu.vironlab.vextension.database.mongo.MongoDatabaseClient
import eu.vironlab.vextension.document.documentFromJson
import java.net.URI
import java.nio.file.Paths

fun CloudUtil.initOnService(featureConfig: BackendFeatureConfiguration) {
    try {
        EventUtil.instance = ServiceGlobalEventProvider()
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
                documentFromJson(
                    CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("dbConnection")
                        .getBlocking()
                        .getValue()
                ).connectionData()
        }.also { it.init() }
        this.featureRegistry = DefaultFeatureRegistry().also {
            if (featureConfig.moderation) {
                it.registerFeature(ServiceModerationFeature::class.java, DefaultServiceModerationFeature())
            }
            if (featureConfig.economy) {
                it.registerFeature(ServiceEconomyFeature::class.java, DefaultServiceEconomyFeature())
            }
            if (featureConfig.broadcast) {
                it.registerFeature(ServiceBroadcastFeature::class.java, DefaultServiceBroadcastFeature())
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}