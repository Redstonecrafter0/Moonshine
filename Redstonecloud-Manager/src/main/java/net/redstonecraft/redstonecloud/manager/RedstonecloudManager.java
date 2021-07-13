package net.redstonecraft.redstonecloud.manager;

import net.redstonecraft.redstoneapi.webserver.WebServer;
import net.redstonecraft.redstonecloud.manager.utils.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;

/**
 * @author Redstonecrafter0
 */
public class RedstonecloudManager {

    public static final Logger LOGGER = Logger.getLogger(RedstonecloudManager.class.getName());

    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                return "[" + this.simpleDateFormat.format(new Date()) + "] [" + record.getLevel().getName() + "] | " + record.getMessage() + "\n";
            }
        });
        LOGGER.addHandler(consoleHandler);
        try {
            FileHandler fileHandler = new FileHandler("logs/latest.log");
            fileHandler.setFormatter(new Formatter() {
                private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    return "[" + this.simpleDateFormat.format(new Date()) + "] [" + record.getLevel().getName() + "] | " + record.getMessage() + "\n";
                }
            });
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final List<Process> processes = new ArrayList<>();
    private final File rootFolder = new File("data");
    private final File templateFolder = new File(rootFolder, "templates");
    private final File runningFolder = new File(rootFolder, "running");
    private final File webFolder = new File(rootFolder, "webroot");
    private final File webTemplates = new File(webFolder, "templates");
    private final File webStatic = new File(webFolder, "static");
    private final Configuration config;
    private final WebServer webServer;

    public RedstonecloudManager(Configuration config) {
        WebServer webServer1;
        this.config = config;
        if (!rootFolder.exists() || !rootFolder.isDirectory()) {
            rootFolder.mkdirs();
        }
        if (!templateFolder.exists() || !rootFolder.isDirectory()) {
            rootFolder.mkdirs();
        }
        if (!runningFolder.exists() || !runningFolder.isDirectory()) {
            runningFolder.mkdirs();
        }
        if (!webFolder.exists() || !webFolder.isDirectory()) {
            webFolder.mkdirs();
            try {
                JarFile jarFile = new JarFile(new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()));
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry i = entries.nextElement();
                    if (i.getName().startsWith("webroot/")) {
                        if (i.isDirectory()) {
                            File tmp = new File(rootFolder, i.getName());
                            if (!tmp.exists() || !tmp.isDirectory()) {
                                tmp.mkdirs();
                            }
                        } else {
                            //noinspection ConstantConditions
                            Files.copy(getClass().getResourceAsStream("/" + i.getName()), new File(rootFolder, i.getName()).toPath());
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
        if (config.webserverEnabled) {
            try {
                webServer1 = new WebServer(config.webserverHost, config.webserverPort, config.webserverLoggging, WebServer.DEFAULT_UNIVERSAL_ERROR_HANDLER, webFolder.getPath());
            } catch (IOException ignored) {
                webServer1 = null;
            }
        } else {
            webServer1 = null;
        }
        webServer = webServer1;
        new Thread(() -> {
            while (true) {
                tick();
            }
        }).start();
    }

    private void tick() {
    }

}
