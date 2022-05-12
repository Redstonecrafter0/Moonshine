package net.redstonecraft.redstonecloud.plugin.proxy

import java.net.InetSocketAddress

data class ServerInfo(val name: String, val socketAddress: InetSocketAddress)

interface IProxy {

    val server: List<ServerInfo>

    fun addServer(server: ServerInfo)
    fun removeServer(server: ServerInfo)

    operator fun plusAssign(server: ServerInfo) = addServer(server)
    operator fun minusAssign(server: ServerInfo) = removeServer(server)

}

class BungeeProxy(private val proxy: net.md_5.bungee.api.ProxyServer): IProxy {

    override val server: List<ServerInfo>
        get() = proxy.servers.map { ServerInfo(it.key, it.value.socketAddress as InetSocketAddress) }

    override fun addServer(server: ServerInfo) {
        proxy.servers += server.name to proxy.constructServerInfo(server.name, server.socketAddress, "", false)
    }

    override fun removeServer(server: ServerInfo) {
        proxy.servers.filter { it.value.socketAddress == server.socketAddress }.keys.toList().forEach { proxy.servers.remove(it) }
    }

}

class VelocityProxy(private val proxy: com.velocitypowered.api.proxy.ProxyServer): IProxy {

    override val server: List<ServerInfo>
        get() = proxy.allServers.map { ServerInfo(it.serverInfo.name, it.serverInfo.address) }

    override fun addServer(server: ServerInfo) {
        proxy.registerServer(com.velocitypowered.api.proxy.server.ServerInfo(server.name, server.socketAddress))
    }

    override fun removeServer(server: ServerInfo) {
        proxy.unregisterServer(proxy.getServer(server.name).get().serverInfo)
    }

}

object DummyProxy: IProxy {
    override val server = emptyList<ServerInfo>()
    override fun addServer(server: ServerInfo) {}
    override fun removeServer(server: ServerInfo) {}
}
