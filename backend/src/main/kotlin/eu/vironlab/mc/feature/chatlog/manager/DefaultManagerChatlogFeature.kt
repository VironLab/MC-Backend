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

package eu.vironlab.mc.feature.chatlog.manager

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.eventapi.IListener
import eu.thesimplecloud.base.manager.startup.Manager
import eu.vironlab.mc.Backend
import eu.vironlab.mc.feature.chatlog.*
import eu.vironlab.mc.feature.chatlog.packet.*
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.database.Database
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import eu.vironlab.vextension.extension.random
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DefaultManagerChatlogFeature(val backend: Backend) : ChatlogFeature, IListener {

    val chatLogMessages: ChatlogMessageConfiguration
    val config: ChatlogConfiguration
    val disconnectedPlayerMessageCache: MutableMap<UUID, MutableList<String>> = mutableMapOf()
    val chatlogStorage: Database = CloudUtil.dbClient.getDatabase("chatlog_chatlogs").complete()

    init {
        this.chatLogMessages = ConfigDocument(File(backend.dataFolder, "chatlog/messages.json")).let {
            it.saveConfig()
            it.get("messages", ChatlogMessageConfiguration::class.java, ChatlogMessageConfiguration())
                .also { itt -> it.saveConfig() }
        }
        this.config = ConfigDocument(File(backend.dataFolder, "chatlog/config.json")).let {
            it.saveConfig()
            it.get("chatlogConfig", ChatlogConfiguration::class.java, ChatlogConfiguration())
                .also { itt -> it.saveConfig() }
        }
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketGetChatlogConfiguration::class.java)
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketCreateChatlog::class.java)
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketGetChatlog::class.java)
        Manager.instance.packetRegistry.registerPacket(
            Backend.instance,
            PacketCacheDisconnectedPlayerMessages::class.java
        )
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketGetDisconnectedPlayerCache::class.java)
    }

    fun addToDisconnectCache(cfg: PlayerChatHistory) {
        this.disconnectedPlayerMessageCache.put(cfg.uuid, cfg.messages)
        GlobalScope.launch {
            delay(TimeUnit.MINUTES.toMillis(config.cacheMinutesAfterDisconnect))
        }
    }

    override fun createChatlog(player: UUID): Chatlog {
        val messages: MutableList<String> =
            CloudAPI.instance.getCloudPlayerManager().getCachedCloudPlayer(player)?.getConnectedProxy()?.let {
                Manager.instance.communicationServer.getClientManager().getClientByClientValue(it)!!
                    .sendQuery(PacketGetChatlogFromProxy(player), PlayerChatHistory::class.java).getBlocking().messages
            } ?: this.disconnectedPlayerMessageCache[player]
            ?: throw IllegalStateException("There is no Data for ${player}")
        var id = String.random(config.idLength)
        while (chatlogStorage.contains(id).complete()) {
            id = String.random(config.idLength)
        }
        val chatlog = Chatlog(id, player, messages)
        this.chatlogStorage.insert(id, document(chatlog)).queue()
        return chatlog
    }

    fun getDisconnectedPlayerCacheData(uuid: UUID): MutableList<String> {
        return this.disconnectedPlayerMessageCache.remove(uuid) ?: mutableListOf()
    }

    override fun getChatlog(id: String): Chatlog? {
        return this.chatlogStorage.get(id).complete()?.toInstance(Chatlog.TYPE)
    }


}