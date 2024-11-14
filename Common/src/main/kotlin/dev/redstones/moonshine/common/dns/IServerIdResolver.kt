package dev.redstones.moonshine.common.dns

import dev.redstones.moonshine.common.token.RoutingToken

interface IServerIdResolver {

    fun resolveRoutingToken(token: RoutingToken): Pair<String, Int>

}

enum class ServerIdResolveStrategy(val resolve: (Set<String>) -> String) {
    Random({ it.random() }),
//    LeastConnections({
//    }), LeastEmptySlots;
}

class K8sServerIdResolver(val resolveStrategy: ServerIdResolveStrategy, val k8sNamespace: String): IServerIdResolver {
    override fun resolveRoutingToken(token: RoutingToken): Pair<String, Int> {
        if (token.serverId != null) {
            return "${token.serverId}.${token.gamemode}.$k8sNamespace.svc" to 25565
        } else {
            val host = "${token.gamemode}.$k8sNamespace.svc"
            val resolvedHosts = DnsResolver.resolve(host).map { it.host }.toSet()
            val selectedHost = resolveStrategy.resolve(resolvedHosts)
            return selectedHost to 25565
        }
    }
}
