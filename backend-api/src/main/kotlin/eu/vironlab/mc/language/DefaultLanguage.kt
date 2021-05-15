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

package eu.vironlab.mc.language

import eu.vironlab.vextension.database.Database
import eu.vironlab.vextension.database.DatabaseClient
import eu.vironlab.vextension.document.Document
import eu.vironlab.vextension.document.document
import java.util.concurrent.atomic.AtomicReference

class DefaultLanguage(override val name: String, val dbClient: DatabaseClient, val prefix: String) : Language {
    private val MESSAGE_VALUE = "message"
    private val tableName: String
    private val messageCache: MutableMap<String, String> = mutableMapOf()
    val storage: Database

    init {
        this.tableName = "language_${name}"
        this.storage = dbClient.getDatabase(tableName).complete()
    }

    override fun getDatabaseName(): String {
        return tableName
    }

    override fun saveMessage(name: String, message: String) {
        if (!storage.contains(name).complete()) {
            storage.insert(name, document(MESSAGE_VALUE, message)).queue()
        }
    }

    override fun updateMessage(name: String, message: String): Boolean {
        if (this.messageCache.containsKey(name)) {
            this.messageCache.remove(name)
        }
        storage.contains(name).queue {
            if (it) {
                storage.update(name, document(MESSAGE_VALUE, message)).complete()
            }
        }
        return true
    }

    override fun getMessage(name: String): String {
        if (messageCache.containsKey(name)) {
            return messageCache.get(name)!!
        }
        if (storage.contains(name).complete()) {
            var optionalMSG: Document = storage.get(name).complete() ?: return name

            var msg: String = optionalMSG.getString(MESSAGE_VALUE)!!
            if (msg.contains("%prefix%")) {
                msg = msg.replace("%prefix%", prefix)
            }
            messageCache.put(name, msg)
            return msg
        }
        return name
    }

    override fun deleteMessage(name: String): Boolean {
        if (storage.contains(name).complete()) {
            storage.delete(name).queue()
            return true
        }
        return false
    }

    override fun containsMessage(name: String): Boolean {
        return storage.contains(name).complete()
    }

    override fun replace(name: String, placeholderData: Document): String {
        if (storage.contains(name).complete()) {
            val s = AtomicReference(getMessage(name))
            if (!placeholderData.isEmpty()) {
                val l: MutableList<String> = mutableListOf()
                l.forEach {}
                placeholderData.forEach {
                    s.set(
                        s.get()!!.replace("%$it%", placeholderData.getString(it)!!)
                    )
                }
            }
            return s.get()!!
        }
        return name
    }
}