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

package eu.vironlab.mc.feature.moderation.service

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.player.ICloudPlayer
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.clientserverapi.lib.packet.packetsender.sendQuery
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise
import eu.thesimplecloud.plugin.startup.CloudPlugin
import eu.vironlab.mc.extension.replace
import eu.vironlab.mc.feature.moderation.*
import eu.vironlab.mc.feature.moderation.chatlog.Chatlog
import eu.vironlab.mc.feature.moderation.packet.punishment.*
import eu.vironlab.mc.feature.moderation.packet.replay.PacketStartReplay
import eu.vironlab.mc.feature.moderation.replay.PlayingReplay
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.documentFromJson
import java.util.*
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder


class DefaultServiceModerationFeature : ServiceModerationFeature {

    val messages: ModerationMessageConfig
    val periodFormatter: PeriodFormatter

    init {
        this.messages = CloudPlugin.instance.connectionToManager.sendQuery<ModerationMessageConfig>(
            PacketGetPunishMessageConfig(),
            ModerationMessageConfig::class.java
        ).getBlocking()
        this.periodFormatter = PeriodFormatterBuilder()
            .appendYears().appendSuffix(" ${messages.times.year}, ", " ${messages.times.years}, ")
            .appendMonths().appendSuffix(" ${messages.times.month}, ", " ${messages.times.months}, ")
            .appendWeeks().appendSuffix(" ${messages.times.week}, ", " ${messages.times.weeks}, ")
            .appendDays().appendSuffix(" ${messages.times.day}, ", " ${messages.times.days}, ")
            .appendHours().appendSuffix(" ${messages.times.hour}, ", " ${messages.times.hours}, ")
            .appendMinutes().appendSuffix(" ${messages.times.minute}, ", " ${messages.times.minutes}, ")
            .appendSeconds().appendSuffix(" ${messages.times.second}", " ${messages.times.seconds}")
            .printZeroNever()
            .toFormatter()
    }

    override fun getKickMessage(reason: String): String =
        this.messages.kickHeader + this.messages.kickMessage.replace(
            document(
                "reason",
                reason
            )
        ) + this.messages.kickFooter


    override fun getBanMessage(punishment: Punishment): String =
        this.messages.kickHeader + this.messages.banMessage.replace(
            document("reason", punishment.reason).append("timeout", dischargeString(punishment.expirationTime))
                .append("id", punishment.id)
        ) + this.messages.kickFooter

    override fun saveReplay(service: ICloudService): ICommunicationPromise<Unit> {
        TODO("Custom Packet to Manager")
    }

    override fun updatePunishments(player: UUID, data: PlayerPunishmentData): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.connectionToManager.sendQuery<Unit>(
            PacketUpdatePunishment(player, data),
            Unit::class.java
        )
    }


    override fun getMuteMessage(punishment: Punishment): String =
        this.messages.muteMessage.replace(
            document("reason", punishment.reason).append("timeout", dischargeString(punishment.expirationTime))
                .append("id", punishment.id)
        )

    fun dischargeString(timeout: Long): String = if (timeout == 0L) {
        messages.permanentExpire
    } else {
        this.periodFormatter.print(Period(System.currentTimeMillis(), timeout))
    }

    override fun getReasons(): ICommunicationPromise<MutableMap<Int, PunishReason>> {
        return CloudPlugin.instance.connectionToManager.sendQuery<String>(PacketGetPunishReasons(), String::class.java)
            .then {
                documentFromJson(it).toInstance(PunishReason.MAP_TYPE)
            }
    }

    override fun getReason(id: Int): ICommunicationPromise<PunishReason> {
        return CloudPlugin.instance.connectionToManager.sendQuery<PunishReason>(
            PacketGetPunishReason(id),
            PunishReason::class.java
        )
    }

    override fun getPunishments(player: UUID): ICommunicationPromise<PlayerPunishmentData> {
        return CloudPlugin.instance.connectionToManager.sendQuery<PlayerPunishmentData>(
            PacketGetPunishments(player),
            PlayerPunishmentData::class.java
        )
    }

    override fun addPunishment(reasonID: Int, executor: String, player: UUID): ICommunicationPromise<String> {
        return CloudPlugin.instance.connectionToManager.sendQuery<String>(
            PacketAddPunishment(
                reasonID,
                executor,
                player
            ), String::class.java
        )
    }

    override fun createChatlog(player: UUID): ICommunicationPromise<Chatlog> {
        TODO("Not yet implemented")
    }

    override fun getChatlog(id: String): ICommunicationPromise<Chatlog> {
        TODO("Not yet implemented")
    }

    override fun playReplay(id: UUID, teleportOnFinish: ICloudPlayer): ICommunicationPromise<PlayingReplay> {
        return CloudPlugin.instance.connectionToManager.sendQuery<SerializedPlayingReplay>(
            PacketStartReplay(
                id, teleportOnFinish
            )
        ).then {
            PlayingReplayImpl(
                it.id,
                it.players,
                CloudAPI.instance.getCloudServiceManager().getCloudServiceByName(it.serviceName)
                    ?: throw IllegalStateException("There is no Service for the received Replay Information"),
                it.serviceName, it.duration,
                it.saved
            )
        }
    }
}