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

package eu.vironlab.mc.feature.economy.manager

import eu.thesimplecloud.api.player.IOfflineCloudPlayer
import eu.thesimplecloud.base.manager.startup.Manager
import eu.vironlab.mc.Backend
import eu.vironlab.mc.feature.economy.EconomyMessageConfiguration
import eu.vironlab.mc.feature.economy.ManagerEconomyFeature
import eu.vironlab.mc.feature.economy.event.CoinUpdateAction
import eu.vironlab.mc.feature.economy.event.UpdateCoinsEvent
import eu.vironlab.mc.feature.economy.packet.PacketGetCoins
import eu.vironlab.mc.feature.economy.packet.PacketUpdateCoins
import eu.vironlab.mc.util.EventUtil
import eu.vironlab.vextension.document.wrapper.ConfigDocument
import java.io.File


class DefaultManagerEconomyFeature(val propertyName: String, val configDir: File) :
    ManagerEconomyFeature {

    val messages: EconomyMessageConfiguration
    val startCoins: Long

    init {
        this.startCoins = ConfigDocument(File(configDir, "config.json")).let {
            it.loadConfig();
            it.getLong("startCoins", 1000L).also { cfg -> it.saveConfig() }
        }
        this.messages = ConfigDocument(File(configDir, "messages.json")).let {
            it.loadConfig(); it.get(
            "messages",
            EconomyMessageConfiguration::class.java,
            EconomyMessageConfiguration()
        ).also { cfg -> it.saveConfig() }
        }
        ManagerPacketCoinsConstant.ecoFeature = this
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketGetCoins::class.java)
        Manager.instance.packetRegistry.registerPacket(Backend.instance, PacketUpdateCoins::class.java)
    }

    override fun getCoins(player: IOfflineCloudPlayer): Long {
        return player.getProperty<Long>(propertyName)?.getValue() ?: player.let {
            it.setProperty<Long>(
                propertyName,
                startCoins
            ).getValue(); it.update(); startCoins
        }
    }

    override fun addCoins(coins: Long, player: IOfflineCloudPlayer) {
        val before = getCoins(player)
        val new = before + coins
        setCoins0(new, player)
        EventUtil.callGlobal(
            UpdateCoinsEvent(
                player.getUniqueId(),
                before,
                new,
                CoinUpdateAction.ADD
            )
        )
    }

    override fun removeCoins(coins: Long, player: IOfflineCloudPlayer) {
        val before = getCoins(player)
        val new = before - coins
        setCoins0(new, player)
        EventUtil.callGlobal(
            UpdateCoinsEvent(
                player.getUniqueId(),
                before,
                new,
                CoinUpdateAction.REMOVE
            )
        )
    }

    override fun setCoins(coins: Long, player: IOfflineCloudPlayer) {
        setCoins0(coins, player)
        EventUtil.callGlobal(
            UpdateCoinsEvent(
                player.getUniqueId(),
                getCoins(player),
                coins,
                CoinUpdateAction.SET
            )
        )
    }

    fun setCoins0(coins: Long, player: IOfflineCloudPlayer) {
        if (player.hasProperty(propertyName)) {
            player.removeProperty(propertyName)
        }
        player.setProperty(propertyName, coins)
        player.update()
    }

}