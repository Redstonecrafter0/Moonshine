package net.redstonecraft.redstonecloud.api.entities;

import java.io.File;

/**
 * @author Redstonecrafter0
 */
public interface ServerInstance {

    ServerInfo getServerInfo();
    File getWorkingDirectory();

}
