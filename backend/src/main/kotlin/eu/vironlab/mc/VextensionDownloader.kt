/**
 * Copyright © 2020 | vironlab.eu | All Rights Reserved.
 *
 *
 *
 *
 * ___    _______                        ______         ______
 *
 *
 * __ |  / /___(_)______________ _______ ___  / ______ ____  /_
 *
 *
 * __ | / / __  / __  ___/_  __ \__  __ \__  /  _  __ `/__  __ \
 *
 *
 * __ |/ /  _  /  _  /    / /_/ /_  / / /_  /___/ /_/ / _  /_/ /
 *
 *
 * _____/   /_/   /_/     \____/ /_/ /_/ /_____/\__,_/  /_.___/
 *
 *
 *
 *
 * ____  _______     _______ _     ___  ____  __  __ _____ _   _ _____
 *
 *
 * |  _ \| ____\ \   / / ____| |   / _ \|  _ \|  \/  | ____| \ | |_   _|
 *
 *
 * | | | |  _|  \ \ / /|  _| | |  | | | | |_) | |\/| |  _| |  \| | | |
 *
 *
 * | |_| | |___  \ V / | |___| |__| |_| |  __/| |  | | |___| |\  | | |
 *
 *
 * |____/|_____|  \_/  |_____|_____\___/|_|   |_|  |_|_____|_| \_| |_|
 *
 *
 *
 *
 *
 *
 * This program is free software: you can redistribute it and/or modify
 *
 *
 * it under the terms of the GNU General Public License as published by
 *
 *
 * the Free Software Foundation, either version 3 of the License, or
 *
 *
 * (at your option) any later version.
 *
 *
 *
 *
 * This program is distributed in the hope that it will be useful,
 *
 *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *
 *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *
 *
 * GNU General Public License for more details.
 *
 *
 *
 *
 * You should have received a copy of the GNU General Public License
 *
 *
 * along with this program.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 *
 *
 *
 * Contact:
 *
 *
 *
 *
 * Discordserver:   https://discord.gg/wvcX92VyEH
 *
 *
 * Website:         https://vironlab.eu/
 *
 *
 * Mail:            contact@vironlab.eu
 *
 *
 *
 *
 */
package eu.vironlab.mc

import eu.vironlab.vextension.dependency.DependencyLoader
import eu.vironlab.vextension.dependency.factory.createDependencyLoader
import eu.vironlab.vextension.document.DocumentFactory
import eu.vironlab.vextension.document.DocumentInit
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Files

internal object VextensionDownloader {
    @JvmStatic
    fun loadVextension(path: File) {
        val urlStr =
            "https://ci.vironlab.eu/job/Vextension/lastSuccessfulBuild/artifact/vextension-common/build/libs/vextension-common.jar"
        val filePath = "vextension/"
        val fileName = "vextension-common.jar"
        val folder = File(path, filePath)
        val dest = File(folder, fileName)
        try {
            if (!dest.exists()) {
                println("Downloading library $fileName !")
                dest.parentFile.mkdirs()
                val requestURL = URL(urlStr)
                Files.copy(requestURL.openStream(), dest.toPath())
            }
            try {
                DependencyClassLoader().addJarToClasspath(dest)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        val dependencyLoader: DependencyLoader = createDependencyLoader(path) {
            addJCenter()
            addMavenCentral()
            addVironLabSnapshot()
        }
        dependencyLoader.download("com.google.inject:guice:5.0.1")
        dependencyLoader.download("joda-time:joda-time:2.9.9")
        DocumentInit.downloadDocumentDependencies(dependencyLoader)
    }

    private class DependencyClassLoader {
        companion object {
            private var addUrl: Method? = null
            @Throws(Exception::class)
            private fun openUrlClassLoaderModule() {
                val moduleClass = Class.forName("java.lang.Module")
                val getModuleMethod = Class::class.java.getMethod("getModule", *arrayOfNulls(0))
                val addOpensMethod = moduleClass.getMethod(
                    "addOpens", *arrayOf(
                        String::class.java, moduleClass
                    )
                )
                val urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader::class.java, *arrayOfNulls(0))
                val thisModule =
                    getModuleMethod.invoke(DependencyClassLoader::class.java, *arrayOfNulls(0))
                addOpensMethod.invoke(
                    urlClassLoaderModule,
                    *arrayOf(URLClassLoader::class.java.getPackage().name, thisModule)
                )
            }

            init {
                try {
                    openUrlClassLoaderModule()
                } catch (throwable: Throwable) {
                }
                try {
                    addUrl = URLClassLoader::class.java.getDeclaredMethod(
                        "addURL", *arrayOf<Class<*>>(
                            URL::class.java
                        )
                    )
                    addUrl!!.isAccessible = true
                } catch (e: NoSuchMethodException) {
                    throw RuntimeException(e)
                }
            }
        }

        private var classLoader: URLClassLoader? = null
        fun addJarToClasspath(paramPath: File) {
            try {
                addUrl!!.invoke(classLoader, *arrayOf<Any>(paramPath.toURI().toURL()))
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException(e)
            } catch (e: MalformedURLException) {
                throw RuntimeException(e)
            }
        }

        init {
            if (javaClass.classLoader is URLClassLoader) {
                classLoader = javaClass.classLoader as URLClassLoader
            } else {
                throw IllegalStateException("ClassLoader is not instance of URLClassLoader")
            }
        }
    }
}