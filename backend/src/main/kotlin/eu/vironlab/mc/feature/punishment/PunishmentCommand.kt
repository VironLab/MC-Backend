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
import eu.thesimplecloud.api.command.ICommandSender
import eu.thesimplecloud.api.player.ICloudPlayer
import eu.thesimplecloud.launcher.console.command.CommandType
import eu.thesimplecloud.launcher.console.command.ICommandHandler
import eu.thesimplecloud.launcher.console.command.annotations.Command
import eu.thesimplecloud.launcher.console.command.annotations.CommandArgument
import eu.thesimplecloud.launcher.console.command.annotations.CommandSubPath
import eu.thesimplecloud.launcher.console.command.provider.CloudPlayerCommandSuggestionProvider
import eu.thesimplecloud.launcher.console.command.provider.ICommandSuggestionProvider
import eu.thesimplecloud.module.permission.player.getPermissionPlayer
import eu.vironlab.mc.Backend
import eu.vironlab.mc.extension.replace
import eu.vironlab.vextension.document.document

@Command("punish", CommandType.INGAME, "backend.cmd.punish", aliases = ["ban", "kick", "mute"])
class PunishmentCommand(val punishFeature: PunishmentFeature, val messageConfig: PunishmentMessageConfig) :
    ICommandHandler {

    val infoMessage: String =
        StringBuilder("${messageConfig.prefix}${messageConfig.availableReasonsHeader}").let { msg ->
            punishFeature.reasons.forEach { (id, reason) ->
                val first = reason.durations.firstOrNull { it.times == 1 }
                    ?: throw IllegalStateException("The Reason with id: $id is invalid")
                msg.append(
                    "\n${messageConfig.prefix}${
                        messageConfig.reasonTemplate.replace(
                            document(
                                "id",
                                id
                            ).append("name", reason.name).append("type", first.type).append("time", first.length)
                                .append("unit", messageConfig.times.fromTimeUnit(first.unit, (first.length == 1L)))
                        )
                    }"
                )
            }
            msg.append("\n${messageConfig.prefix} /punish <user> <id>")
            msg.toString()
        }

    init {
        punishFeature.reasons.forEach { id, value ->
            PunishIdSuggestionProvider.ids.add("$id")
        }
    }

    @CommandSubPath
    fun sendInfo(sender: ICommandSender) {
        sender.sendMessage(infoMessage)
    }

    @CommandSubPath("<user>")
    fun handleUser(
        sender: ICommandSender,
        @CommandArgument("user", PunishUserArgSuggestionProvider::class) user: String
    ) {
        sendInfo(sender)
    }

    @CommandSubPath("info <user>")
    fun sendPlayerInfo(
        sender: ICommandSender,
        @CommandArgument("user", CloudPlayerCommandSuggestionProvider::class) user: String
    ) {
    }

    @CommandSubPath("<user> <id>")
    fun addPunishment(
        sender: ICommandSender,
        @CommandArgument("user", CloudPlayerCommandSuggestionProvider::class) user: String,
        @CommandArgument("id", PunishIdSuggestionProvider::class) idStr: String
    ) {
        if (sender is ICloudPlayer) {
            if (sender.getName().equals(user, true)) {
                sender.sendMessage(messageConfig.prefix + messageConfig.cantPunishOwn)
                return
            }
        }

        val id = idStr.toIntOrNull() ?: run {
            sender.sendMessage(
                messageConfig.prefix + Backend.instance.messages.interferedType.replace(
                    document(
                        "given",
                        "Text"
                    ).append("requested", "Number")
                )
            )
            return
        }
        val reason = punishFeature.getReason(id) ?: run {
            sender.sendMessage(messageConfig.prefix + messageConfig.reasonNotExists.replace("%reason%", "$id"))
            return
        }
        reason.permission?.let {
            if (!sender.hasPermissionSync(reason.permission!!)) {
                sender.sendMessage(
                    messageConfig.prefix + messageConfig.reasonNotAllowed.replace(
                        "%reason%",
                        reason.name
                    )
                )
                return@addPunishment
            }
        }
        val target = CloudAPI.instance.getCloudPlayerManager().getOfflineCloudPlayer(user).getBlockingOrNull() ?: run {
            sender.sendMessage(messageConfig.prefix + Backend.instance.messages.playerNotExist.replace("%name%", user))
            return@addPunishment
        }
        reason.ignorePermission?.let {
            if (target.getPermissionPlayer().hasPermission(it)) {
                sender.sendMessage(
                    messageConfig.prefix + messageConfig.targetIgnoreReason.replace(
                        document("target", target.getDisplayName()).append("reason", reason.name)
                    )
                )
                return@addPunishment
            }
        }
        val punishId = punishFeature.addPunishment(
            id,
            if (sender is ICloudPlayer) sender.getUniqueId().toString() else "System", target
        )
        sender.sendMessage(
            messageConfig.prefix + messageConfig.punishSuccess.replace(
                document(
                    "target",
                    target.getDisplayName()
                ).append("id", punishId).append("reason", reason.name)
            )
        )
    }
}

class PunishUserArgSuggestionProvider : ICommandSuggestionProvider {
    override fun getSuggestions(sender: ICommandSender, fullCommand: String, lastArgument: String): List<String> {
        return mutableListOf("info").let { list ->
            list.addAll(
                CloudAPI.instance.getCloudPlayerManager().getAllCachedObjects().map { it.getName() }); list
        }
    }
}

class PunishIdSuggestionProvider : ICommandSuggestionProvider {
    override fun getSuggestions(sender: ICommandSender, fullCommand: String, lastArgument: String): List<String> {
        return ids
    }

    companion object {
        @JvmStatic
        val ids: MutableList<String> = mutableListOf()
    }

}