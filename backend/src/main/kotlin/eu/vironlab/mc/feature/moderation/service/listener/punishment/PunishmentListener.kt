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

package eu.vironlab.mc.feature.moderation.service.listener.punishment

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import eu.thesimplecloud.api.eventapi.CloudEventHandler
import eu.thesimplecloud.api.eventapi.IListener
import eu.vironlab.mc.feature.moderation.PlayerPunishmentData
import eu.vironlab.mc.feature.moderation.PunishType
import eu.vironlab.mc.feature.moderation.Punishment
import eu.vironlab.mc.feature.moderation.ServiceModerationFeature
import java.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component


class PunishmentListener(val moderationFeature: ServiceModerationFeature) : IListener {

    val mutes: MutableMap<UUID, MutableList<Punishment>> = mutableMapOf()

    @Subscribe(order = PostOrder.FIRST)
    fun handleJoin(e: LoginEvent) {
        check(e.player.uniqueId, type = arrayOf(PunishType.BAN, PunishType.PERMA_BAN))?.let {
            e.result = ResultedEvent.ComponentResult.denied(Component.text(moderationFeature.getBanMessage(it)))
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    fun handleMessage(e: PlayerChatEvent) {
        if (mutes[e.player.uniqueId]?.isNotEmpty() == true) {
            check(
                e.player.uniqueId,
                mutes[e.player.uniqueId]!!,
                update = false,
                PunishType.MUTE,
                PunishType.PERMA_MUTE
            )?.let {
                e.player.sendMessage(Component.text(moderationFeature.getMuteMessage(it)))
                e.result = PlayerChatEvent.ChatResult.denied()
            }
        }
    }

    private fun check(
        player: UUID,
        punishments: MutableList<Punishment> = moderationFeature.getPunishments(player).getBlocking().punishments.toMutableList(),
        update: Boolean = true,
        vararg type: PunishType
    ): Punishment? {
        if (punishments.isEmpty()) {
            return null
        }
        val activePunishments = punishments.filter { it.active }
        GlobalScope.launch {
            val mutes: MutableList<Punishment> = mutableListOf()
            var changed = false
            punishments.forEach {
                if (it.active && it.expirationTime < System.currentTimeMillis()) {
                    it.active = false
                    changed = true
                    this@PunishmentListener.mutes[player]?.find { pun -> pun.id == it.id }
                        ?.active = false
                } else if ((it.type == PunishType.MUTE || it.type == PunishType.PERMA_MUTE) && update)
                    handlePunishmentAdd(eu.vironlab.mc.feature.moderation.event.PunishmentAddEvent(it, player))
            }
            if (update && changed) {
                this@PunishmentListener.mutes[player] = mutes
                moderationFeature.updatePunishments(player, PlayerPunishmentData(punishments))
            }
        }
        val validPunishments = activePunishments.filter { type.contains(it.type) }.toMutableList()
        validPunishments.sortWith { b, a ->
            a.executionTime.compareTo(b.executionTime)
        }
        if (validPunishments.isNotEmpty())
            validPunishments[0].let {
                if (it.expirationTime > System.currentTimeMillis() || it.type.permanent)
                    return it
            }
        return null
    }

    @CloudEventHandler
    fun handlePunishmentUpdate(e: eu.vironlab.mc.feature.moderation.event.PunishmentUpdateEvent) {
        for (punishment in e.punishment)
            when (punishment.type) {
                PunishType.MUTE, PunishType.PERMA_MUTE ->
                    if (!punishment.active)
                        mutes[e.target]!!.removeIf { punishment.id == it.id }
                    else if (!mutes[e.target]!!.any { it.id == punishment.id }) mutes[e.target]!!.add(punishment)
            }
    }

    @CloudEventHandler
    fun handlePunishmentAdd(e: eu.vironlab.mc.feature.moderation.event.PunishmentAddEvent) {
        when (e.punishment.type) {
            PunishType.MUTE, PunishType.PERMA_MUTE -> {
                if (mutes[e.target] == null)
                    mutes[e.target] = mutableListOf()
                mutes[e.target]?.find { pun -> pun.id == e.punishment.id }?.let {
                    mutes[e.target]!!.remove(it)
                }
                mutes[e.target]!!.add(e.punishment)
            }

        }
    }

    @Subscribe
    fun handleQuit(e: DisconnectEvent) {
        this.mutes.remove(e.player.uniqueId)
    }


}