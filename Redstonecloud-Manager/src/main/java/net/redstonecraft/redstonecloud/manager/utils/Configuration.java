package net.redstonecraft.redstonecloud.manager.utils;

import net.redstonecraft.redstoneapi.json.JSONObject;

/**
 * @author Redstonecrafter0
 */
public class Configuration {

    public final boolean webserverEnabled;
    public final String webserverHost;
    public final int webserverPort;
    public final boolean webserverLoggging;

    public Configuration(JSONObject jsonObject) {
        webserverEnabled = jsonObject.getObject("webserver").getBoolean("enabled");
        webserverHost = jsonObject.getObject("webserver").getString("host");
        webserverPort = jsonObject.getObject("webserver").getInt("port");
        webserverLoggging = jsonObject.getObject("webserver").getBoolean("logging");
    }

}
