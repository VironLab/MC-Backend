package eu.vironlab.mc.bukkit.menu

import eu.vironlab.mc.bukkit.BukkitLoader
import eu.vironlab.vextension.inventory.bukkit.BukkitGUI
import org.bukkit.entity.Player

class PlayerMenu(val loader: BukkitLoader) {

    val mainMenu = BukkitGUI(3, "")

    fun open(player: Player) {
        player.sendMessage("§cComming Soon")
    }

}
