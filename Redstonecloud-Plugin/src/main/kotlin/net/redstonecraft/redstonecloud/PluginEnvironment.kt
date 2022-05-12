package net.redstonecraft.redstonecloud

enum class PluginEnvironment(val isProxy: Boolean = false) {
    BUKKIT, SPONGE, MINESTOM, KRYPTON, BUNGEE(true), VELOCITY(true);
}
