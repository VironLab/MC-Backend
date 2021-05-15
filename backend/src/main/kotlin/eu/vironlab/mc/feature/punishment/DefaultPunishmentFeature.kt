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
import eu.vironlab.mc.extension.online
import eu.vironlab.mc.feature.punishmet.PunishmentFeature
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.database.Database
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import eu.vironlab.vextension.extension.random
import java.io.File

class DefaultPunishmentFeature(val cloudUtil: CloudUtil, configDir: File) : PunishmentFeature {

    val config: ConfigDocument
    val reasonDatabase: Database = cloudUtil.dbClient.getDatabase("punish_reasons").complete()
    val punishmentDatabase: Database = cloudUtil.dbClient.getDatabase("punish_punishments").complete()
    val kickHeader: String
    val kickFooter: String

    init {
        this.config = ConfigDocument(File(configDir, "config.json"))
        config.loadConfig()
        this.kickHeader = config.getString(
            "kickHeader",
            "\n§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━ \n\n"
        )
        this.kickFooter =
            config.getString("kickFooter", "\n\n§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━")
    }

    override fun getKickMessage(reason: String, player: IOfflineCloudPlayer) {
        TODO("Not yet implemented")
    }

    override fun getBanMessage(reason: String, timeout: Long, player: IOfflineCloudPlayer) {
        TODO("Not yet implemented")
    }

    override fun getMuteMessage(reason: String, timeout: Long, player: IOfflineCloudPlayer) {
        TODO("Not yet implemented")
    }

    override fun getReasons(id: Int): Reason? {
        return reasonDatabase.get(id.toString()).complete()?.toInstance(Reason.TYPE)
    }

    override fun getPunishments(player: IOfflineCloudPlayer): Collection<Punishment> {
        val data = punishmentDatabase.get(player.getUniqueId().toString()).complete() ?: return mutableListOf()
        return data.getDocument("punishments")?.toInstance(Punishment.COLLECTION_TYPE) ?: run {
            val rs = mutableListOf<Punishment>()
            if (data.contains("punishments")) {
                data.delete("punishments")
            }
            data.append("punishments", rs)
            punishmentDatabase.update(player.getUniqueId().toString(), data).complete()
            rs
        }
    }


    override fun addPunishment(reasonID: Int, executor: String, player: IOfflineCloudPlayer): String {
        val reason = getReasons(reasonID) ?: throw IllegalStateException("Cannot Punish member for unknown reason")
        val punishments = getPunishments(player).toMutableList()
        var id = String.random(6)
        while (punishments.any { it.id == id }) {
            id = String.random(6)
        }
        val timesCount: Int = punishments.filter { it.reason.equals(reason.name, true) }.size
        val times = reason.times.getOrNull(timesCount) ?: reason.times.last()
        punishments.add(Punishment(id, reason.name, executor, true, times.points))
        return id
    }
}
