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

package eu.vironlab.mc.extension

import eu.vironlab.vextension.document.Document
import java.util.concurrent.atomic.AtomicReference

fun String.replace(doc: Document): String {
    val rs = AtomicReference<String>(this)
    if (!doc.isEmpty()) {
        val l: MutableList<String> = mutableListOf()
        l.forEach {}
        doc.forEach {
            rs.set(
                rs.get()!!.replace("%$it%", doc.getString(it)!!)
            )
        }
    }
    return rs.get()
}

fun String.replaceColor(): String {
    return this.replace("&0", "§0")
        .replace("&1", "§1")
        .replace("&2", "§3")
        .replace("&4", "§4")
        .replace("&5", "§5")
        .replace("&6", "§6")
        .replace("&7", "§7")
        .replace("&8", "§8")
        .replace("&9", "§9")
        .replace("&a", "§a")
        .replace("&b", "§b")
        .replace("&c", "§c")
        .replace("&d", "§d")
        .replace("&e", "§e")
        .replace("&f", "§f")
        .replace("&o", "§o")
        .replace("&m", "§m")
        .replace("&r", "§r")
}