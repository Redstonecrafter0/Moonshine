package net.redstonecraft.redstonecloud.manager;

import net.redstonecraft.redstoneapi.tools.StringUtils;
import net.redstonecraft.redstoneapi.webserver.WebServer;
import net.redstonecraft.redstonecloud.manager.commands.StopCommand;
import net.redstonecraft.redstonecloud.manager.entities.ServerInfoImpl;
import net.redstonecraft.redstonecloud.manager.terminal.CommandManager;
import net.redstonecraft.redstonecloud.manager.terminal.CustomPrintStream;
import net.redstonecraft.redstonecloud.manager.terminal.TerminalHandler;
import net.redstonecraft.redstonecloud.manager.utils.Configuration;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;
import java.util.logging.Formatter;

/**
 * @author Redstonecrafter0
 */
public class RedstonecloudManager {

    public static final Logger LOGGER = Logger.getLogger(RedstonecloudManager.class.getName());

    private static RedstonecloudManager INSTANCE;

    static {
        LOGGER.setUseParentHandlers(false);
        Formatter formatter = new Formatter() {
            private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            @Override
            public String format(LogRecord record) {
                String now = simpleDateFormat.format(new Date());
                List<String> lines = new ArrayList<>();
                if (record.getThrown() != null) {
                    Collections.addAll(lines, StringUtils.stringFromError(record.getThrown()).split("\n"));
                }
                lines.add(record.getMessage());
                StringBuilder sb = new StringBuilder();
                for (String i : lines) {
                    sb.append("[").append(now).append("] [").append(record.getLevel().getName()).append("] | ").append(i).append("\n");
                }
                return sb.toString();
            }
        };
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(formatter);
        LOGGER.addHandler(consoleHandler);
        try {
            FileHandler fileHandler = new FileHandler("logs/latest.log");
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.getLogger("org.jline").setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(formatter);
        Logger.getLogger("org.jline").addHandler(ch);
    }

    private final Map<String, Map<Integer, Process>> processes = new LinkedHashMap<>();
    private final File rootFolder = new File("data");
    private final File templateFolder = new File(rootFolder, "templates");
    private final File runningFolder = new File(rootFolder, "running");
    private final File webFolder = new File(rootFolder, "webroot");
    public final File pluginsFolder = new File(rootFolder, "plugins");
    private final Configuration config;
    private final Terminal terminal;
    private final LineReader lineReader;
    private final TerminalHandler terminalHandler;
    private final CommandManager commandManager;
    private final WebServer webServer;
//    private final PacketServer packetServer;

    private boolean run = true;

    public RedstonecloudManager(Configuration config) throws Throwable {
        INSTANCE = this;
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
        if (config.webserverEnabled && (!webFolder.exists() || !webFolder.isDirectory())) {
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
        commandManager = new CommandManager();
        terminal = TerminalBuilder.builder().system(true).encoding(StandardCharsets.UTF_8).build();
        lineReader = LineReaderBuilder.builder().terminal(terminal).completer(commandManager).build();
        System.setOut(new CustomPrintStream(System.out, lineReader));
        System.setErr(new CustomPrintStream(System.err, lineReader));
        terminalHandler = new TerminalHandler(lineReader, commandManager);
        commandManager.registerCommands(null,
                new StopCommand()
        );
        ServerInfoImpl.baseDir = templateFolder;
        WebServer webServer1;
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
            while (run) {
                tick();
            }
        }).start();
    }

    private void tick() {
        for (Map.Entry<String, Map<Integer, Process>> i : processes.entrySet()) {
            for (Map.Entry<Integer, Process> j : i.getValue().entrySet()) {
                if (!j.getValue().isAlive()) {
                }
            }
        }
    }

    public void shutdown() {
        LOGGER.info("Stopping...");
        terminalHandler.stop();
        try {
            terminal.close();
        } catch (Throwable ignored) {
        }
        try {
            webServer.stop();
        } catch (Throwable ignored) {
        }
        run = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        System.exit(0);
    }

    public static RedstonecloudManager getInstance() {
        return INSTANCE;
    }

    public void dispatchCommand(String message) {
        commandManager.performCommand(message);
    }

}
