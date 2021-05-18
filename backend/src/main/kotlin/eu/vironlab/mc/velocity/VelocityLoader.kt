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

package eu.vironlab.mc.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.ProxyServer
import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.VextensionDownloader
import eu.vironlab.mc.extension.initOnService
import eu.vironlab.mc.feature.BackendFeatureConfiguration
import eu.vironlab.mc.feature.punishment.ServicePunishmentFeature
import eu.vironlab.mc.feature.punishment.service.listener.PunishmentListener
import eu.vironlab.mc.util.CloudUtil
import java.io.File
import java.net.URI
import org.slf4j.Logger

class VelocityLoader @Inject constructor(val server: ProxyServer, val logger: Logger) {

    init {
        VextensionDownloader.loadVextension(
            File(
                URI(
                    CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("vextensionLibDir")
                        .getBlocking()
                        .getValue() ?: throw IllegalStateException("Cannot find Module")
                )
            )
        )
        logger.info("=== Backend by VironLab - https://github.com/VironLab ===")
    }

    @Subscribe
    fun init(event: ProxyInitializeEvent) {
        val featureConfig =
            CloudAPI.instance.getGlobalPropertyHolder().requestProperty<BackendFeatureConfiguration>("features")
                .getBlocking().getValue()
        CloudUtil.initOnService(featureConfig)
        if (featureConfig.punishment) {
            CloudUtil.featureRegistry.getFeature(ServicePunishmentFeature::class.java)?.let {
                val punishListener = PunishmentListener(it)
                server.eventManager.register(this, punishListener)
                CloudAPI.instance.getEventManager()
                    .registerListener(CloudAPI.instance.getThisSidesCloudModule(), punishListener)
            }
        }
    }


}