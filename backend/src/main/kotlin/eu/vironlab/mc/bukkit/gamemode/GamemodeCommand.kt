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

package eu.vironlab.mc.bukkit.gamemode

import eu.vironlab.mc.config.BackendMessageConfiguration
import eu.vironlab.mc.extension.replace
import eu.vironlab.vextension.document.document
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class GamemodeCommand(val messages: GameModeMessageConfiguration, val backendMessages: BackendMessageConfiguration) :
    CommandExecutor {

    val usage = messages.usageTemplate.replace("%usage%", "/gamemode <player> <gamemode>")

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("backend.cmd.gamemode")) {
            sender.sendMessage(messages.noAllowed)
            return true
        }
        val target = if (args.size == 2) {
            if (!sender.hasPermission("backend.cmd.gamemode.other")) {
                sender.sendMessage(messages.prefix + messages.cannotChangeOther)
                return true
            }
            args[1]
        } else {
            sender.name
        }
        if (args.size == 1 || args.size == 2) {
            val targetPlayer = Bukkit.getPlayer(target) ?: run {
                sender.sendMessage(messages.prefix + backendMessages.playerNotExist.replace("%name%", target))
                return true
            }
            var modeName = ""
            val gamemode: GameMode = when (args[0]) {
                "0", messages.gamemodes.survival -> {
                    modeName = messages.gamemodes.survival
                    GameMode.SURVIVAL
                }
                "1", messages.gamemodes.creative -> {
                    modeName = messages.gamemodes.creative
                    GameMode.CREATIVE
                }
                "2", messages.gamemodes.adventure -> {
                    modeName = messages.gamemodes.adventure
                    GameMode.ADVENTURE
                }
                "3", messages.gamemodes.spectator -> {
                    modeName = messages.gamemodes.spectator
                    GameMode.SPECTATOR
                }
                else -> {
                    sender.sendMessage(messages.prefix + messages.gamemodeNotFound.replace("%mode%", args[0]))
                    return true
                }
            }.also {
                val str = ("backend.cmd.gamemode.${if (args.size == 2) "other." else ""}${it.name.toLowerCase()}")
                if (!sender.hasPermission(str)) {
                    sender.sendMessage(messages.prefix + backendMessages.permissionMissing.replace("%permission%", str))
                    return true
                }
            }
            targetPlayer.gameMode = gamemode
            sender.sendMessage(
                messages.prefix + if (targetPlayer.name == sender.name) {
                    messages.changed.replace("%mode%", modeName)
                } else {
                    targetPlayer.sendMessage(
                        messages.prefix + messages.changed.replace("%mode%", modeName)
                    )
                    messages.changedOther.replace(
                        document("name", targetPlayer.name).append(
                            "mode",
                            modeName
                        )
                    )
                }
            )
        } else {
            sender.sendMessage(messages.prefix + messages.usageTemplate)
        }
        return true
    }
}
