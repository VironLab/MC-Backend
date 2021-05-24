package eu.vironlab.mc.bukkit.replay.world

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.concurrent.Executors
import java.util.zip.CRC32
import org.bukkit.Bukkit

class WorldChecker(val saveDir: File) {

    init {
        val worldNames = Bukkit.getWorlds().map { it.name }
        Executors.newCachedThreadPool().execute {
            worldNames.forEach { world ->
                val worldFile = File(world)
                val digest = writeDirToDigest(worldFile)
                val value = digest.value
                val targetFiles = saveDir.listFiles().filter { it.name.startsWith(world) }
                var save: Boolean = true
                if (targetFiles.isNotEmpty()) {
                    for (targetFile in targetFiles) {
                        if (writeDirToDigest(targetFile).value == value) {
                            save = false
                            break
                        }
                    }
                }
                if (save) {
                    Bukkit.getConsoleSender().sendMessage("Try to copy world: ${world} in Replay Storage")
                    val start = System.currentTimeMillis()
                    Files.copy(worldFile.toPath(), File(saveDir, "${world}-${(targetFiles.size + 1)}").toPath())
                    Bukkit.getConsoleSender()
                        .sendMessage("Copy of World: ${world} finished in ${(System.currentTimeMillis() - start)}ms")
                }
            }
        }
    }

    private fun writeDirToDigest(dir: File, digest: CRC32 = CRC32()): CRC32 {
        val files = dir.listFiles().also { it.sortBy { file -> file.name } }
        for (file in files) {
            if (file.isDirectory) {
                writeDirToDigest(dir, digest)
            } else {
                digest.update(FileInputStream(file).readAllBytes())
            }
        }
        return digest
    }

}