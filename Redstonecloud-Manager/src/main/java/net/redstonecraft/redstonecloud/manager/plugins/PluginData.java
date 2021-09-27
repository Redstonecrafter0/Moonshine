package net.redstonecraft.redstonecloud.manager.plugins;

import net.redstonecraft.redstoneapi.json.JSONObject;
import net.redstonecraft.redstoneapi.json.parser.JSONParser;
import net.redstonecraft.redstoneapi.tools.Version;

import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

/**
 * @author Redstonecrafter0
 */
public class PluginData {

    private final String name;
    private final Version version;
    private final Class<? extends RedstonecloudPlugin> mainClass;

    public PluginData(JarFile jarFile) throws Exception {
        JSONObject object = JSONParser.parseObject(new String(jarFile.getInputStream(jarFile.getJarEntry("rcloud.json")).readAllBytes(), StandardCharsets.UTF_8));
        name = object.getString("name");
        version = new Version(object.getString("version"));
        mainClass = (Class<? extends RedstonecloudPlugin>) Class.forName(object.getString("main"));
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public Class<? extends RedstonecloudPlugin> getMainClass() {
        return mainClass;
    }

}
