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

import eu.thesimplecloud.api.player.IOfflineCloudPlayer
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.database.Database
import eu.vironlab.vextension.document.document
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import eu.vironlab.vextension.extension.random
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder
import java.io.File
import java.util.concurrent.TimeUnit

class DefaultPunishmentFeature(val cloudUtil: CloudUtil, configDir: File) : PunishmentFeature {

    val config: ConfigDocument = ConfigDocument(File(configDir, "config.json"))
    val reasonConfig: ConfigDocument = ConfigDocument(File(configDir, "reasons.json"))
    val reasons: MutableMap<Int, PunishReason> = mutableMapOf()
    val punishmentDatabase: Database = cloudUtil.dbClient.getDatabase("punish_punishments").complete()
    val kickHeader: String
    val kickFooter: String
    val permaMutePoints: Int
    val permaBanPoints: Int

    init {
        config.loadConfig()
        reasonConfig.loadConfig()
        this.permaMutePoints = config.getInt("permaMutePoints", 20)
        this.permaBanPoints = config.getInt("permaBanPoints", 20)
        this.kickHeader = config.getString(
            "kickHeader",
            "\n§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━ \n\n"
        )
        this.kickFooter =
            config.getString("kickFooter", "\n\n§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━")
        initReasons()
    }

    override fun getKickMessage(reason: String, player: IOfflineCloudPlayer): String =
        this.kickHeader + cloudUtil.languageProvider.getLanguage(player)
            .replace("punish.kick", document("reason", reason)) + this.kickFooter


    override fun getBanMessage(reason: String, expiration: Long, player: IOfflineCloudPlayer): String =
        this.kickHeader + cloudUtil.languageProvider.getLanguage(player).replace(
            "punish.ban",
            document("reason", reason).append("timeout", dischargeString(expiration, player))
        ) + this.kickFooter

    override fun getMuteMessage(reason: String, expiration: Long, player: IOfflineCloudPlayer): String =
        cloudUtil.languageProvider.getLanguage(player).replace(
            "punish.mute",
            document("reason", reason).append("timeout", dischargeString(expiration, player))
        )

    private fun dischargeString(timeout: Long, player: IOfflineCloudPlayer): String
        = PeriodFormatterBuilder()
            .appendYears().appendSuffix(" year, ", " years, ")
            .appendMonths().appendSuffix(" month, ", " months, ")
            .appendWeeks().appendSuffix(" week, ", " weeks, ")
            .appendDays().appendSuffix(" day, ", " days, ")
            .appendHours().appendSuffix(" hour, ", " hours, ")
            .appendMinutes().appendSuffix(" minute, ", " minutes, ")
            .appendSeconds().appendSuffix(" second", " seconds")
            .printZeroNever()
            .toFormatter().print(Period(timeout, System.currentTimeMillis()))


    override fun getReason(id: Int): PunishReason? = this.reasons[id]

    override fun getPunishments(player: IOfflineCloudPlayer): Collection<Punishment> =
        punishmentDatabase.get(player.getUniqueId().toString()).complete()?.toInstance(Punishment.COLLECTION_TYPE)
            ?: mutableListOf()

    override fun addPunishment(reasonID: Int, executor: String, player: IOfflineCloudPlayer): String {
        val reason = getReason(reasonID) ?: throw IllegalStateException("Invalid Reason ID!")
        val punishments = getPunishments(player).toMutableList()
        val punishmentsOfTypeCount = punishments.count { it.reason == reason.name }
        var id = String.random(6)
        while (punishments.any { it.id == id }) id = String.random(6)
        val duration = reason.durations.getOrNull(punishmentsOfTypeCount) ?: reason.durations.last()
        punishments.add(Punishment(id, true, executor, reason.name, duration.type, System.currentTimeMillis(), System.currentTimeMillis() + duration.unit.toMillis(duration.length)))
        punishmentDatabase.update(player.getUniqueId().toString(), document(punishments))
        return id
    }

    private fun initReasons() { // TODO "ADD DEFAULT REASONS"
        this.reasonConfig.getDocument("reasons", document()).getKeys().forEach {
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
            reasons[id] = PunishReason(id, name, durations)
        }
    }

}
