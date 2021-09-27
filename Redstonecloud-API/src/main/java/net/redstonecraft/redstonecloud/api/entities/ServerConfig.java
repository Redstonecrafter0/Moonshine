package net.redstonecraft.redstonecloud.api.entities;

/**
 * @author Redstonecrafter0
 */
public interface ServerConfig {

    ServerInfo getServerInfo();
    ServerHandling getServerHandling();
    int getMaxInstances();
    int getMaxPlayers();
    String getJrePath();
    String[] getJvmArgs();
    String getJar();
    String[] getArgs();

}
