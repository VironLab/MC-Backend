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

package eu.vironlab.mc.feature.broadcast

import com.google.gson.reflect.TypeToken
import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class DefaultBroadcastFeature(
    messagesFile: File,
    val cloudUtil: CloudUtil = CloudUtil,
) {

    val config: ConfigDocument = ConfigDocument(messagesFile).also { it.loadConfig() }
    val format: String

    init {
        config.getString(
            "format",
            "§8=====================================================\n\n §c§lBROADCAST §7➜ %message% \n\n§8====================================================="
        )
        config.getLong("delay", 5)
        config.get(
            "messages",
            object : TypeToken<MutableList<String>>() {}.type,
            mutableListOf(
                "Join our §3Discord: https://discord.gg/J5FX39UGjP",
                "SourceCode of our Network: §ehttps://github.com/VironLab"
            )
        )
        config.saveConfig()
        this.format = config.getString("format")!!
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
            {
                val currentBroadcast =
                    config.get<MutableList<String>>("messages", object : TypeToken<MutableList<String>>() {}.type)!!
                        .random()
                CloudAPI.instance.getCloudPlayerManager().getAllOnlinePlayers().getBlocking().forEach { player ->
                    player.getCloudPlayer().getBlocking().sendMessage(
                        this.format.replace(
                            "%message%",
                            currentBroadcast
                        )
                    )
                }
            }, 0, config.getLong("delay")!!, TimeUnit.MINUTES
        )
    }

}