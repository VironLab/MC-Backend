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

import eu.vironlab.vextension.document.Document
import eu.vironlab.vextension.lang.Nameable

interface Language : Nameable {

    /**
     * Use this method to get the Database
     *
     * @return the name of the Database in wich is the language stored
     */
    fun getDatabaseName(): String

    /**
     * Saves a Message in the Database if its not exists
     *
     * @param name is the Name of the Message an the Key to get it
     * @param message is the Message wich will be stored
     */
    fun saveMessage(name: String, message: String)

    /**
     * Update an already stored Message
     *
     * @param name is the Name of the Message
     * @param message is the new Message to Insert into the database
     * @return if the replacement was successful
     */
    fun updateMessage(name: String, message: String): Boolean

    /**
     * Get a Message stored in the Database
     * ${prefix} is the Default placeholder for the Network Prefix and automatically replaced every time
     *
     * @param name is the Name of the Message
     * @return the Message if exists otherwise return null
     */
    fun getMessage(name: String): String

    /**
     * Delete a Message for the Language
     *
     * @param name is the Name of the Message
     * @return if the delete was successful
     */
    fun deleteMessage(name: String): Boolean

    /**
     * Check if a Message is present in the Database
     *
     * @param name is the Name of the Message
     * @return if the Message is Present
     */
    fun containsMessage(name: String): Boolean

    /**
     * Get a Message an automatically replace the Placeholders
     * ${prefix} is the Default placeholder for the Network Prefix and automatically replaced every time
     *
     * @param name is the Name of the Message
     * @param placeholderData is a JsonDocument with all Placeholder the Key is the Placeholder excluded thy "%" and "%"
     * @return the Message with the replaced Placeholders
     */
    fun replace(name: String, placeholderData: Document): String
}