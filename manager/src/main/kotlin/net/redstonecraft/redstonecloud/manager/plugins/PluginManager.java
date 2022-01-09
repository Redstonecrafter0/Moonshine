package net.redstonecraft.redstonecloud.manager.plugins;

import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * @author Redstonecrafter0
 */
public class PluginManager {

    private final Map<String, RedstonecloudPlugin> plugins = new HashMap<>();

    public RedstonecloudPlugin getPlugin(String name) {
        return plugins.get(name);
    }

    public void reloadPlugins() {
        for (Map.Entry<String, RedstonecloudPlugin> i : plugins.entrySet()) {
            try {
                i.getValue().onDisable();
            } catch (Throwable e) {
                RedstonecloudManager.LOGGER.log(Level.WARNING, "Error while disabling plugin " + i.getKey() + ".", e);
            }
        }
        plugins.clear();
        //noinspection ConstantConditions
        for (File i : RedstonecloudManager.getInstance().pluginsFolder.listFiles()) {
            if (i.getName().endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(i);
                    PluginData pluginData = new PluginData(jarFile);
                    try {
                        Class<? extends RedstonecloudPlugin> mainClass = null;
                        RedstonecloudPlugin plugin = mainClass.getDeclaredConstructor().newInstance();
                        plugin.setPluginData(pluginData);
                        plugin.setup();
                        plugins.put(pluginData.getName(), plugin);
                    } catch (Throwable e) {
                        RedstonecloudManager.LOGGER.log(Level.WARNING, "Error while loading plugin " + pluginData.getName() + ".", e);
                    }
                } catch (Throwable e) {
                    RedstonecloudManager.LOGGER.log(Level.WARNING, "Error while loading plugin.", e);
                }
            }
        }
        List<String> errored = new ArrayList<>();
        for (Map.Entry<String, RedstonecloudPlugin> i : plugins.entrySet()) {
            try {
                i.getValue().onEnable();
            } catch (Throwable e) {
                errored.add(i.getKey());
                RedstonecloudManager.LOGGER.log(Level.WARNING, "Error while enabling plugin " + i.getKey() + ".", e);
            }
        }
        errored.forEach(plugins::remove);
    }

}
