package net.redstonecraft.redstonecloud.manager.entities;

import net.redstonecraft.redstoneapi.tools.StringUtils;
import net.redstonecraft.redstonecloud.api.entities.ServerConfig;
import net.redstonecraft.redstonecloud.api.entities.ServerInfo;
import net.redstonecraft.redstonecloud.api.entities.ServerInstance;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Redstonecrafter0
 */
public class ServerInfoImpl implements ServerInfo {

    public static File baseDir;

    private String name;

    public ServerInfoImpl(String name) {
        if (!StringUtils.isValid(name, "qwertzuiopasdfghjklyxcvbnmQWERTZUIOPASDFGHJKLYXCVBNM-_".toCharArray())) {
            throw new IllegalArgumentException("Invalid name");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Path getSourcePath() {
        return new File(baseDir, name).toPath();
    }

    @Override
    public ServerConfig getConfig() {
        return null;
    }

    @Override
    public ServerInstance createInstance() {
        return null;
    }

}
