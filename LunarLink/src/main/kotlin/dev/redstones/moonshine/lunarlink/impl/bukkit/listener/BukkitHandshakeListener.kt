package dev.redstones.moonshine.lunarlink.impl.bukkit.listener

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory
import com.comphenix.protocol.wrappers.WrappedChatComponent
import dev.redstones.moonshine.common.token.RoutingToken
import dev.redstones.moonshine.lunarlink.impl.bukkit.LunarLinkBukkit

class BukkitHandshakeListener(plugin: LunarLinkBukkit): PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Handshake.Client.SET_PROTOCOL) {

    override fun onPacketReceiving(event: PacketEvent) {
        val packet = event.packet
        val nextState = packet.protocols.readSafely(0) ?: return disconnect(event, "packet error")
        if (nextState == PacketType.Protocol.STATUS) {
            return // only verify on login and transfer. transfer is missing from protocollib
        }
        val host = packet.strings.readSafely(0) ?: return disconnect(event, "packet error")
        val verifier = RoutingToken.Verifier.randomVerifier()
        if (verifier.verify(host) == null) {
            disconnect(event, "unauthorized")
        }
    }

    private fun disconnect(event: PacketEvent, message: String) {
        val component = WrappedChatComponent.fromText(message)
        val packet = PacketContainer(PacketType.Login.Server.DISCONNECT)
        packet.modifier.writeDefaults()
        packet.chatComponents.write(0, component)
        ProtocolLibrary.getProtocolManager().sendServerPacket(event.player, packet)
        TemporaryPlayerFactory.getInjectorFromPlayer(event.player).disconnect(message)
    }

}
