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

package eu.vironlab.mc.feature.punishment.command

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
import eu.vironlab.mc.feature.punishment.ManagerPunishmentFeature
import eu.vironlab.mc.feature.punishment.PunishmentMessageConfig
import eu.vironlab.mc.feature.punishment.PlayerPunishmentData
import eu.vironlab.vextension.extension.toCleanString
import java.util.*

@Command("unpunish", CommandType.INGAME, aliases = ["unban", "unmute", "pardon"])
class UnpunishCommand(val punishFeature: ManagerPunishmentFeature, val messageConfig: PunishmentMessageConfig) :
    ICommandHandler {

    init {
        UnpunishListIds.punishFeature = punishFeature
    }

    @CommandSubPath("<player> <id>")
    fun unpunish(
        sender: ICommandSender,
        argsRaw: Array<String>,
        @CommandArgument("player", CloudPlayerCommandSuggestionProvider::class) playerStr: String,
        @CommandArgument("id", UnpunishListIds::class) id: String
    ) {
        if (!sender.hasPermissionSync("backend.cmd.unpunish")) {
            sender.sendMessage(
                messageConfig.prefix + Backend.instance.messages.permissionMissing.replace(
                    "%permission%",
                    "backend.cmd.unpunish"
                )
            )
            return
        }
        val args = Arrays.copyOfRange(argsRaw, 2, argsRaw.size)
        if (args.isEmpty()) {
            sender.sendMessage(messageConfig.prefix + messageConfig.noUnpunishReason)
            return
        }
        val player =
            CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(playerStr).getBlockingOrNull() ?: run {
                sender.sendMessage(
                    messageConfig.prefix + Backend.instance.messages.playerNotExist.replace(
                        "%name%",
                        playerStr
                    )
                ) // INVALID PLAYER
                return
            }
        val punishments = punishFeature.getPunishments(player.getUniqueId()).punishments.toMutableList()
        val punishment = punishments.firstOrNull { it.id == id } ?: run {
            sender.sendMessage(messageConfig.prefix + messageConfig.invalidId.replace("%id%", id))
            return
        }
        if (!punishment.active) {
            sender.sendMessage(messageConfig.prefix + messageConfig.punishmentInactive.replace("%id%", id))
            return
        }
        punishment.active = false
        punishment.unPunishReason = args.toCleanString()
        punishment.unPunishExecutor = if (sender is ICloudPlayer) sender.getUniqueId().toString() else "System"
        punishFeature.updatePunishments(player.getUniqueId(), PlayerPunishmentData(punishments))
        sender.sendMessage(
            messageConfig.prefix + messageConfig.unpunishSuccess.replace("%player%", playerStr).replace("%id%", id)
        )
    }
}

class UnpunishListIds : ICommandSuggestionProvider {
    override fun getSuggestions(sender: ICommandSender, fullCommand: String, lastArgument: String): List<String> {
        if (!sender.hasPermissionSync("backend.cmd.unpunish")) {
            sender.sendMessage("LOLLLLL")
            return mutableListOf()
        }
        sender.sendMessage(fullCommand.split(" ")[1])
        sender.sendMessage((CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(fullCommand.split(" ")[1]).getBlockingOrNull() == null).toString())
        return (CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(fullCommand.split(" ")[1])
            .getBlockingOrNull() ?: run { return mutableListOf() }).let {
            punishFeature.getPunishments(it.getUniqueId()).punishments.map { punishment -> punishment.id }
        }
    }

    companion object {
        @JvmStatic
        lateinit var punishFeature: ManagerPunishmentFeature
    }

}
