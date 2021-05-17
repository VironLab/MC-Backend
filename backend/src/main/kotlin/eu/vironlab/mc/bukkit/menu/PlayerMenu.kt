package eu.vironlab.mc.bukkit.menu

import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.bukkit.BukkitLoader
import eu.vironlab.vextension.inventory.bukkit.BukkitGUI
import eu.vironlab.vextension.item.Material
import eu.vironlab.vextension.item.builder.createItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class PlayerMenu(val loader: BukkitLoader) {

    val messages: PlayerMenuMessageConfiguration = CloudAPI.instance.getGlobalPropertyHolder()
        .requestProperty<PlayerMenuMessageConfiguration>("playerMenuMessages").getBlocking().getValue()
    val mainMenu = BukkitGUI(3, this.messages.mainMenu.title).also {
        it.setItem(11, createItem(Material.IRON_CHESTPLATE) {
            this.setName(messages.mainMenu.team)
            this.setUnbreakable(true)
            this.setLore(messages.mainMenu.teamLore)
            this.setBlockAll(true)
            this.setPermission("backend.playermenu.team")
            this.setClickHandler { item, playerId ->
                Bukkit.getPlayer(playerId)?.sendMessage("Hello World")
            }
        })
    }

    fun open(player: Player) {
        mainMenu.open(player.uniqueId) { player ->
            setItem(13, createItem(Material.PLAYER_HEAD) {
                this.skullOwner = Bukkit.getPlayer(player)!!.name
                this.setName(messages.mainMenu.profile)
                this.setUnbreakable(true)
                this.setLore(messages.mainMenu.profileLore)
                this.setBlockAll(true)
                this.setClickHandler { item, playerId ->
                    Bukkit.getPlayer(playerId)?.sendMessage("§cComming Soon")
                }
            }.also {})
        }
    }

}
