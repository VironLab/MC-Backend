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

package eu.vironlab.mc.feature.economy

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.command.ICommandSender
import eu.thesimplecloud.api.player.ICloudPlayer
import eu.thesimplecloud.launcher.console.command.CommandType
import eu.thesimplecloud.launcher.console.command.ICommandHandler
import eu.thesimplecloud.launcher.console.command.annotations.Command
import eu.thesimplecloud.launcher.console.command.annotations.CommandArgument
import eu.thesimplecloud.launcher.console.command.annotations.CommandSubPath
import eu.thesimplecloud.launcher.console.command.provider.CloudPlayerCommandSuggestionProvider
import eu.thesimplecloud.launcher.console.command.provider.ICommandSuggestionProvider
import eu.vironlab.mc.Backend
import eu.vironlab.mc.extension.replace
import eu.vironlab.vextension.document.document

@Command("coins", CommandType.INGAME, aliases = ["money"])
class EconomyCommand(val economyFeature: ManagerEconomyFeature, val messages: EconomyMessageConfiguration) : ICommandHandler {

    @CommandSubPath
    fun showCoins(sender: ICommandSender) {
        if (sender !is ICloudPlayer) {
            sender.sendMessage("§cONLY FOR PLAYERS")
            return
        }
        sender.sendMessage(
            messages.prefix + messages.coinInfo.replace(
                "%coins%",
                economyFeature.getCoins(sender).toString()
            )
        )
    }

    @CommandSubPath("<action> <player> <coins>")
    fun setCoins(
        sender: ICommandSender,
        @CommandArgument("action", CoinActionSuggestionProvider::class) action: String,
        @CommandArgument("player", CloudPlayerCommandSuggestionProvider::class) player: String,
        @CommandArgument("coins") coinsStr: String
    ) {
        if (!sender.hasPermissionSync("backend.cmd.eco.edit")) {
            sender.sendMessage(
                messages.prefix + Backend.instance.messages.permissionMissing.replace(
                    "%permission%",
                    "backend.cmd.eco.edit"
                )
            )
            return
        }
        val coins = coinsStr.toLongOrNull() ?: run {
            sender.sendMessage(
                messages.prefix + Backend.instance.messages.interferedType.replace(
                    document(
                        "given",
                        "Text"
                    ).append("requested", "Number")
                )
            )
            return
        }
        val target =
            CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(player).getBlockingOrNull() ?: run {
                sender.sendMessage(messages.prefix + Backend.instance.messages.playerNotExist.replace("%name%", player))
                return@setCoins
            }
        when (action.toLowerCase()) {
            "add" -> {
                economyFeature.addCoins(coins, target);
                sender.sendMessage(
                    messages.prefix + messages.addCoinsMessage.replace(
                        document(
                            "name",
                            target.getDisplayName()
                        ).append("coins", coins)
                    )
                )
            }
            "remove" -> {
                economyFeature.removeCoins(coins, target)
                sender.sendMessage(
                    messages.prefix + messages.addCoinsMessage.replace(
                        document(
                            "name",
                            target.getDisplayName()
                        ).append("coins", coins)
                    )
                )
            }
            "set" -> {
                economyFeature.setCoins(coins, target)
                sender.sendMessage(
                    messages.prefix + messages.addCoinsMessage.replace(
                        document(
                            "name",
                            target.getDisplayName()
                        ).append("coins", coins)
                    )
                )
            }
            else -> {
                sender.sendMessage(messages.prefix + messages.editUsage)
                return
            }
        }
    }
}

class CoinActionSuggestionProvider : ICommandSuggestionProvider {
    override fun getSuggestions(sender: ICommandSender, fullCommand: String, lastArgument: String): List<String> {
        return if (sender.hasPermissionSync("backend.cmd.eco.edit")) {
            mutableListOf<String>("set", "remove", "add")
        } else {
            mutableListOf<String>()
        }
    }
}