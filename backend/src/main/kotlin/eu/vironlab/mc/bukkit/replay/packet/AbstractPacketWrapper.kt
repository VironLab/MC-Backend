package eu.vironlab.mc.bukkit.replay.packet

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

abstract class AbstractPacketWrapper(
    val wrapped: PacketContainer,
    val type: PacketType,
    val protocolManager: ProtocolManager = ProtocolLibrary.getProtocolManager()
) {

    fun send(player: Player) = player.sendServerPacket(wrapped)

    fun receive(player: Player) = player.receiveClientPacket(wrapped)

}

fun Player.receiveClientPacket(container: PacketContainer) {
    ProtocolLibrary.getProtocolManager().recieveClientPacket(this, container)
}

fun Player.sendServerPacket(container: PacketContainer) {
    ProtocolLibrary.getProtocolManager().sendServerPacket(this, container)
}