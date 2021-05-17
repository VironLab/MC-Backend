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

package eu.vironlab.mc.bukkit

import eu.thesimplecloud.api.CloudAPI
import eu.vironlab.mc.VextensionDownloader
import eu.vironlab.mc.bukkit.gamemode.GameModeMessageConfiguration
import eu.vironlab.mc.bukkit.gamemode.GamemodeCommand
import eu.vironlab.mc.bukkit.menu.PlayerMenu
import eu.vironlab.mc.bukkit.menu.PlayerMenuListenerCommand
import eu.vironlab.mc.config.BackendMessageConfiguration
import eu.vironlab.mc.extension.initOnService
import eu.vironlab.mc.util.CloudUtil
import eu.vironlab.vextension.item.bukkit.BukkitItemEventConsumer
import java.io.File
import java.net.URI
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class BukkitLoader : JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: BukkitLoader

        @JvmStatic
        lateinit var key: NamespacedKey
    }

    lateinit var cloudUtil: CloudUtil
    lateinit var cfg: BukkitConfiguration
    lateinit var backendMessages: BackendMessageConfiguration

    override fun onEnable() {
        CloudUtil.initOnService()
        logger.info("Loaded Vextension by VironLab: https://github.com/VironLab/Vextension")
        Bukkit.getPluginManager().registerEvents(BukkitItemEventConsumer(), this)
        this.backendMessages = CloudAPI.instance.getGlobalPropertyHolder()
            .requestProperty<BackendMessageConfiguration>("backendMessageConfig").getBlocking().getValue()
        this.cfg = CloudAPI.instance.getGlobalPropertyHolder().requestProperty<BukkitConfiguration>("bukkitConfig")
            .getBlocking().getValue()
        if (cfg.playermenu) {
            val menu = PlayerMenu(this)
            val menuListener = PlayerMenuListenerCommand(menu)
            getCommand("menu")!!.setExecutor(menuListener)
            Bukkit.getPluginManager().registerEvents(menuListener, this)
        }
        if (cfg.gamemode) {
            getCommand("gamemode")!!.setExecutor(
                GamemodeCommand(
                    CloudAPI.instance.getGlobalPropertyHolder()
                        .requestProperty<GameModeMessageConfiguration>("gamemodeConfig").getBlocking().getValue(),
                    this.backendMessages
                )
            )
        }
    }

    val items: MutableMap<String, ItemStack> = mutableMapOf()


    override fun onLoad() {
        VextensionDownloader.loadVextensionBukkit(
            File(
                URI(
                    CloudAPI.instance.getGlobalPropertyHolder().requestProperty<String>("vextensionLibDir")
                        .getBlocking()
                        .getValue() ?: throw IllegalStateException("Cannot find Module")
                )
            )
        )
        instance = this
        key = NamespacedKey(instance, "backend")
    }


}