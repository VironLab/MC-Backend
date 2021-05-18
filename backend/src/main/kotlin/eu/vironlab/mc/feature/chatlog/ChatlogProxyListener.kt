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

package eu.vironlab.mc.feature.chatlog

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import eu.thesimplecloud.base.wrapper.startup.Wrapper
import eu.thesimplecloud.clientserverapi.lib.packet.IPacket
import eu.thesimplecloud.clientserverapi.lib.packetresponse.WrappedResponseHandler
import eu.thesimplecloud.clientserverapi.lib.packetresponse.responsehandler.IPacketResponseHandler
import eu.thesimplecloud.clientserverapi.lib.promise.CommunicationPromise
import java.util.*


class ChatlogProxyListener(val config: ChatlogConfiguration) {

    val messageCache: MutableMap<UUID, MutableList<String>> = mutableMapOf()

    init {
        Wrapper.instance.communicationClient.getPacketResponseManager()
            .registerResponseHandler(UUID.randomUUID(), WrappedResponseHandler(
                object : IPacketResponseHandler<PlayerChatHistory> {
                    override fun handleResponse(packet: IPacket): PlayerChatHistory? {
                        TODO("Not yet implemented")
                    }
                }, CommunicationPromise<PlayerChatHistory>(0L, false)
            )
            )
    }

    @Subscribe(order = PostOrder.LATE)
    fun handleChatMessage(e: PlayerChatEvent) {
        val messages = messageCache.get(e.player.uniqueId)
        if (messages == null) {
            this.messageCache.put(e.player.uniqueId, mutableListOf(e.message))
            return
        }
        while (messages.size >= config.savedMessageCount) {
            messages.removeFirst()
        }
        messages.add(e.message)
    }


}