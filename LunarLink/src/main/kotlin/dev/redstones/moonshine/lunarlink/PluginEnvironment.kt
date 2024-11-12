package dev.redstones.moonshine.lunarlink

enum class PluginEnvironment(val isProxy: Boolean = false) {
    BUKKIT, SPONGE, KRYPTON, BUNGEE(true), VELOCITY(true);
}
