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

package eu.vironlab.mc.feature.punishment

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.module.permission.PermissionPool
import eu.vironlab.mc.extension.replace
import eu.vironlab.mc.feature.punishment.event.PunishmentAddEvent
import eu.vironlab.mc.feature.punishment.event.PunishmentUpdateEvent
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.mc.util.EventUtil
import eu.vironlab.vextension.database.Database
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import eu.vironlab.vextension.extension.random
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

class DefaultPunishmentFeature(val cloudUtil: CloudUtil, configDir: File) : PunishmentFeature {

    val messages: PunishmentMessageConfig
    val reasonConfig: ConfigDocument = ConfigDocument(File(configDir, "reasons.json")).let {
        it.getDocument("reasons", document())
        it
    }
    override val reasons: MutableMap<Int, PunishReason> = mutableMapOf()
    val punishmentDatabase: Database = cloudUtil.dbClient.getDatabase("punish_punishments").complete()
    val periodFormatter: PeriodFormatter

    init {
        reasonConfig.loadConfig()
        initReasons()
        this.messages = ConfigDocument(File(configDir, "messages.json")).let { cfg ->
            cfg.loadConfig(); cfg.get(
            "messages",
            PunishmentMessageConfig::class.java,
            PunishmentMessageConfig()
        ).also { cfg.saveConfig() }
        }
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

    override fun updatePunishments(player: UUID, punishments: PlayerPunishmentData) {
        val data = document(punishments)
        if (!punishmentDatabase.update(player.toString(), data)
                .complete()
        ) {
            punishmentDatabase.insert(player.toString(), data).complete()
        }
        EventUtil.callGlobal(PunishmentUpdateEvent(punishments.punishments, player))
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


    override fun getReason(id: Int): PunishReason? = this.reasons[id]

    override fun getPunishments(player: UUID): PlayerPunishmentData {
        return punishmentDatabase.get(player.toString()).complete()?.toInstance(PlayerPunishmentData.TYPE)
            ?: PlayerPunishmentData(
                mutableListOf()
            )
    }

    override fun addPunishment(reasonID: Int, executor: String, player: UUID): String {
        val reason = getReason(reasonID) ?: throw IllegalStateException("Invalid Reason ID!")
        if (reason.ignorePermission != null) {
            if (PermissionPool.instance.getPermissionPlayerManager().getPermissionPlayer(player)
                    .getBlocking().hasPermission(reason.ignorePermission!!)
            ) {
                return "NOT PUNISHED -> IGNORE PERMISSION GIVEN"
            }
        }
        val punishments = getPunishments(player).punishments.toMutableList()
        val punishmentsOfTypeCount = punishments.count { it.reason == reason.name }
        var id = String.random(6)
        while (punishments.any { it.id == id }) id = String.random(6)
        val duration = reason.durations.getOrNull(punishmentsOfTypeCount) ?: reason.durations.last()
        val punishment = Punishment(
            id,
            true,
            executor,
            reason.name,
            duration.type,
            System.currentTimeMillis(),
            if (!duration.type.permanent) System.currentTimeMillis() + duration.unit.toMillis(duration.length)
            else 0L
        )
        punishments.add(
            punishment
        )
        val data = document(PlayerPunishmentData(punishments))
        if (!punishmentDatabase.update(player.toString(), data)
                .complete()
        ) {
            punishmentDatabase.insert(player.toString(), data).complete()
        }
        EventUtil.callGlobal(PunishmentAddEvent(punishment, player))
        CloudAPI.instance.getCloudPlayerManager().getCachedCloudPlayer(player)?.let {
            when (duration.type) {
                PunishType.BAN, PunishType.PERMA_BAN -> it.kick(getBanMessage(punishment))
                PunishType.MUTE, PunishType.PERMA_MUTE -> it.sendMessage(getMuteMessage(punishment))
                PunishType.WARN -> it.kick(getKickMessage(punishment.reason))
            }
        }

        return id
    }

    private fun initReasons() {
        this.reasonConfig.getDocument(
            "reasons",
            document().append(
                "1",
                document("name", "Hacking").append(
                    "durations",
                    document(
                        "1",
                        document("length", 30L).append("unit", TimeUnit.DAYS.toString())
                            .append("type", PunishType.BAN.toString())
                    )
                )
            ).append(
                "2", document("name", "Provocation").append(
                    "durations", document(
                        "1",
                        document("length", 1L).append("unit", TimeUnit.DAYS.toString())
                            .append("type", PunishType.MUTE.toString())
                    )
                ).append("ignorePermission", "backend.punish.provocation.ignore")
                    .append("permission", "backend.punish.provocation")
            )
        ).getKeys().forEach {
            val id: Int = it.toIntOrNull() ?: throw IllegalStateException("Id have to be an number")
            val data = reasonConfig.getDocument("reasons")!!.getDocument(it)!!
            val name = data.getString("name") ?: throw IllegalStateException("Name is missing on Reason: $id")
            val durations: MutableList<PunishDuration> = mutableListOf()
            data.getDocument("durations")?.forEach { durationString ->
                data.getDocument("durations")!!.getDocument(durationString).let { duration ->
                    durations.add(
                        PunishDuration(
                            durationString.toIntOrNull()!!,
                            duration!!.getLong("length")!!,
                            TimeUnit.valueOf(duration.getString("unit")!!),
                            PunishType.valueOf(duration.getString("type")!!)
                        )
                    )
                }
            } ?: throw IllegalStateException("Duration is missing on Reason: $id")
            reasons[id] =
                PunishReason(id, name, durations, data.getString("ignorePermission"), data.getString("permission"))
        }
        this.reasonConfig.saveConfig()
    }

}
