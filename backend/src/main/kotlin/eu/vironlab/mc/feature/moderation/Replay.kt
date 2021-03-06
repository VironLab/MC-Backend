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

package eu.vironlab.mc.feature.moderation

import com.google.gson.reflect.TypeToken
import eu.thesimplecloud.api.service.ICloudService
import eu.vironlab.mc.feature.moderation.replay.PlayingReplay
import eu.vironlab.mc.feature.moderation.replay.Replay
import java.lang.reflect.Type
import java.util.*


data class SerializedReplay(
    override val id: UUID, override val players: MutableList<UUID>,
    override val serviceGroup: String, override val duration: Long, override val saved: Long,
    override val worlds: MutableList<UUID>
) : Replay {
    companion object {
        @JvmStatic
        val TYPE: Type = object : TypeToken<SerializedReplay>() {}.type
    }
}

data class PlayingReplayImpl(
    override val id: UUID,
    override val players: MutableList<UUID>, override val service: ICloudService, override val serviceGroup: String,
    override val duration: Long,
    override val saved: Long, override val worlds: MutableList<UUID>
) : PlayingReplay {

}

data class SerializedPlayingReplay(
    val id: UUID,
    val players: MutableList<UUID>,
    val serviceName: String,
    val duration: Long,
    val saved: Long,
    val worlds: MutableList<UUID>
) {
    companion object {
        @JvmStatic
        fun fromInterface(replay: PlayingReplay): SerializedPlayingReplay {
            return SerializedPlayingReplay(
                replay.id,
                replay.players,
                replay.service.getName(),
                replay.duration,
                replay.saved,
                replay.worlds
            )
        }
    }
}
