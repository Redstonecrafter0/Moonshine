package net.redstonecraft.redstonecloud.plugin

enum class PluginEnvironment(val isProxy: Boolean = false) {
    BUKKIT, SPONGE, KRYPTON, BUNGEE(true), VELOCITY(true);
}
