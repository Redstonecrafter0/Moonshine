# LunarLink

is a plugin for various Minecraft server implementations which adds the capabilities to work with
and understand the features outlined in [Gateway](../Gateway/README.md).

## Additional Features
- Implements custom messaging for LuckPerms to use Moonshines Unicast Messages.

## Supported Minecraft Server Implementations
- Bukkit 1.8+
  - Spigot 1.8+
    - Paper 1.8+
      - Purpur 1.21.1+
- Minestom (TBD)
- Sponge (TBD)
- Bungeecord 1.16+
  - ~~Waterfall 1.16+~~ deprecated
- Velocity 3.4.0+

## Known incompatibilities
For certain features like the encryption mentioned [here](../Gateway/README.md) other plugins may be incompatible if they need more control
than the Bukkit and other APIs provide. Especially plugins interfering with the protocol are susceptible to compatibility issues but there are way around that.
For example plugins MUST NOT check for the class of a players socket object.

- None yet