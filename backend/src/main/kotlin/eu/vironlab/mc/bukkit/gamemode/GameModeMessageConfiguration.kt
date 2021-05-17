package eu.vironlab.mc.bukkit.gamemode

import eu.vironlab.mc.util.CloudUtil

class GameModeMessageConfiguration {

    val prefix = CloudUtil.prefix
    val changed = "You are now §a%mode%"
    val changedOther = "You set §2%name%§7 in §a%mode%"
    val cannotChangeOther = "You cannot change the Gamemode of other Players"
    val noAllowed = "You are not allowed to change Gamemodes"
    val usageTemplate = "Please use %usage%"
    val gamemodes: Gamemodes = Gamemodes()

    class Gamemodes {
        val survival = "survival"
        val creative = "creative"
        val adventure = "adventure"
        val spectator = "spectator"
    }

}
