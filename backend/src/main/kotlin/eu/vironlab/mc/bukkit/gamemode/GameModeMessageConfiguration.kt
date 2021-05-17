package eu.vironlab.mc.bukkit.gamemode

import eu.vironlab.mc.util.CloudUtil

class GameModeMessageConfiguration {

    val prefix = CloudUtil.prefix
    val changed = "You are now in §a%mode%§7 Mode"
    val changedOther = "You set §2%name%§7 in §a%mode%§7 Mode"
    val cannotChangeOther = "You cannot change the Gamemode of other Players"
    val noAllowed = "You are not allowed to change Gamemodes"
    val gamemodeNotFound = "The Gamemode §2%mode% §7was not found"
    val usageTemplate = "Please use §2/gamemode <0|1|2|3> <Player>"
    val noAllowedMode = "You cannot use the Gamemode: §2%mode%"
    val gamemodes: Gamemodes = Gamemodes()

    class Gamemodes {
        val survival = "survival"
        val creative = "creative"
        val adventure = "adventure"
        val spectator = "spectator"
    }

}
