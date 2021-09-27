package net.redstonecraft.redstonecloud.manager.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Redstonecrafter0
 */
public class PluginClassLoader extends URLClassLoader {

    PluginClassLoader(PluginLoader loader, ClassLoader parent, PluginData pluginData, File file) throws IOException {
        super(new URL[]{file.toURI().toURL()}, parent);
    }

}
