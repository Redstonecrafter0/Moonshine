package net.redstonecraft.redstonecloud.manager.terminal;

import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;

/**
 * @author Redstonecrafter0
 */
public class TerminalHandler {

    private static final String PROMPT = ">";
    private final CommandManager commandManager;
    private boolean run = true;

    private final LineReader lineReader;
    private final Thread thread = new Thread(() -> {
        while (run) {
            tick();
        }
    });

    public TerminalHandler(LineReader lineReader, CommandManager commandManager) {
        this.lineReader = lineReader;
        this.commandManager = commandManager;
        thread.start();
    }

    private void tick() {
        try {
            String[] lines = lineReader.readLine(PROMPT).split("\n");
            for (String i : lines) {
                if (i.length() > 0) {
                    commandManager.performCommand(i);
                }
            }
        } catch (UserInterruptException | EndOfFileException ignored) {
            RedstonecloudManager.getInstance().shutdown();
        }
    }

    public void stop() {
        run = false;
    }

}
