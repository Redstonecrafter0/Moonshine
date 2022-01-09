package net.redstonecraft.redstonecloud.manager.plugins;

import net.redstonecraft.redstoneapi.json.JSONObject;
import net.redstonecraft.redstoneapi.json.parser.JSONParser;
import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author Redstonecrafter0
 */
public abstract class RedstonecloudPlugin {

    private JSONObject config = null;
    private PluginData pluginData = null;
    private File configFile = null;

    void setup() {
        configFile = new File(getPluginDirectory(), "config.json");
        try {
            config = JSONParser.parseObject(new String(new FileInputStream(configFile).readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    void setPluginData(PluginData pluginData) {
        this.pluginData = pluginData;
    }

    public abstract void onLoad();

    public abstract void onEnable();

    public abstract void onDisable();

    public final PluginData getPluginData() {
        return pluginData;
    }

    public final File getPluginDirectory() {
        return new File(RedstonecloudManager.getInstance().pluginsFolder, getPluginData().getName());
    }

    public JSONObject getConfig() {
        return config;
    }

    public void saveConfig() throws IOException {
        try (FileOutputStream os = new FileOutputStream(configFile)) {
            os.write(config.toPrettyJsonString().getBytes(StandardCharsets.UTF_8));
        }
    }

}
