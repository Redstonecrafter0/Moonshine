package net.redstonecraft.redstonecloud.api.entities;

import java.nio.file.Path;

/**
 * @author Redstonecrafter0
 */
public interface ServerInfo {

    String getName();
    Path getSourcePath();
    ServerConfig getConfig();
    ServerInstance createInstance();

}
