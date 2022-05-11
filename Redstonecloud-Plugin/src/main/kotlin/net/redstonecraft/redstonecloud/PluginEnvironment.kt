package net.redstonecraft.redstonecloud

enum class PluginEnvironment(val isProxy: Boolean = true) {
    BUKKIT(false), SPONGE(false), MINESTOM(false), KRYPTON(false), BUNGEE, VELOCITY;
}
