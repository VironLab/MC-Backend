package eu.vironlab.mc.feature.punishment

import eu.vironlab.mc.util.CloudUtil
import java.util.concurrent.TimeUnit


class PunishmentMessageConfig {

    val prefix: String = CloudUtil.prefix
    val kickHeader = "§2§lViron§a§lLab §8■ §2Open Source Network \n\n"
    val kickFooter = "\n\n\n§2Join our Discord: https://discord.gg/J5FX39UGjP"
    val kickMessage = "§2You got kicked \n\n §2Reason: §a%reason%"
    val banMessage = "§2You got banned \n\n §2Reason: §a%reason% \n §2Expire: §a%timeout% \n §2ID: §7#§a%id%"
    val muteMessage = "§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━ \n\n" +
            "§8│§c Muted §8➜ §a §2Reason: §a%reason% §2Expire: §a%timeout% §2ID: §7#§a%id%" +
            "\n\n§8§m━━━━━━━━━━━━━━━━━━━━§8[§2§lViron§a§lLab§8]§m━━━━━━━━━━━━━━━━━━━━"
    val availableReasonsHeader = "Available Reasons"
    val reasonTemplate = "§2%id% §8- §a%name% §8- §a%type% §8- §a%time% §8- §a%unit%"
    val reasonNotAllowed = "You are not allowed to use the Reason: §2%reason%"
    val reasonNotExists = "The Reason with id: §c%reason% §7does not exists"
    val targetIgnoreReason = "You cannot Punish §2%target% §7with Reason §2%reason%"
    val cantPunishOwn = "You cannot Punish yourself"
    val punishSuccess = "You Punished §2%target%§7 for §a%reason%§7 Id: §a%id%"
    val cannotAccessInfo = "You cant Access the Info of a User"
    val infoHeader = "Info for %name%"
    val noPunishments = "The Player §2%name% §7has no Punishments"
    val invalidId = "The ID #%id% is invalid for this player"
    val unpunishSuccess = "You successfully unpunished §2%player%§7, §2Id: §a%id%"
    val infoTemplate = "§2%id% §8- §a%reason% §8- §a%type% §8- §a%active%"
    val punishmentInactive = "The Id §2%id% §7is already inactive"
    val noUnpunishReason = "You have to give a reason for unpunish that Player"
    val permanent = "Permanent"
    val times: Times = Times()

    class Times {
        val year = "Year"
        val years = "Years"
        val month = "Month"
        val months = "Months"
        val week = "Week"
        val weeks = "Weeks"
        val day = "Day"
        val days = "Days"
        val hour = "Hour"
        val hours = "Hours"
        val minute = "Minute"
        val minutes = "Minutes"
        val second = "Second"
        val seconds = "Seconds"

        fun fromTimeUnit(unit: TimeUnit, singular: Boolean): String {
            return when (unit) {
                TimeUnit.DAYS -> if (singular) { this.day } else { this.days }
                TimeUnit.HOURS -> if (singular) { this.hour } else { this.hours }
                TimeUnit.MINUTES -> if (singular) { this.minute } else { this.minutes }
                TimeUnit.SECONDS -> if (singular) { this.second } else { this.seconds }
                else -> "TimeUnit not Supportet"
            }
        }
    }

}
