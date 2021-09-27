package net.redstonecraft.redstonecloud.manager.entities;

import net.redstonecraft.redstoneapi.json.JSONObject;
import net.redstonecraft.redstoneapi.json.parser.JSONParser;
import net.redstonecraft.redstonecloud.api.entities.ServerConfig;
import net.redstonecraft.redstonecloud.api.entities.ServerHandling;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Redstonecrafter0
 */
public class ServerConfigImpl implements ServerConfig {

    private final ServerInfoImpl serverInfo;
    private final ServerHandling serverHandling;
    private final int maxInstances;
    private final int maxPlayers;
    private final String jrePath;
    private final String[] jvmArgs;
    private final String jar;
    private final String[] args;

    public ServerConfigImpl(ServerInfoImpl serverInfo) throws IOException, IllegalArgumentException {
        this.serverInfo = serverInfo;
        try (FileInputStream is = new FileInputStream(new File(ServerInfoImpl.baseDir, serverInfo.getName() + "/server_config.json"))) {
            JSONObject config = JSONParser.parseObject(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            serverHandling = ServerHandling.valueOf(config.getString("handling"));
            maxInstances = config.getInt("maxInstances");
            maxPlayers = config.getInt("maxPlayers");
            jrePath = config.getString("jre_path");
            jvmArgs = ((List<String>) config.getArray("jvm_args")).toArray(new String[0]);
            jar = config.getString("jar");
            args = ((List<String>) config.getArray("args")).toArray(new String[0]);
        }
    }

    @Override
    public ServerInfoImpl getServerInfo() {
        return serverInfo;
    }

    @Override
    public ServerHandling getServerHandling() {
        return serverHandling;
    }

    @Override
    public int getMaxInstances() {
        return maxInstances;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public String getJrePath() {
        return jrePath;
    }

    @Override
    public String[] getJvmArgs() {
        return jvmArgs;
    }

    @Override
    public String getJar() {
        return jar;
    }

    @Override
    public String[] getArgs() {
        return args;
    }

}
