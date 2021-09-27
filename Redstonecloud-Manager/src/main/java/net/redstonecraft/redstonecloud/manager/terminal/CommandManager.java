package net.redstonecraft.redstonecloud.manager.terminal;

import net.redstonecraft.redstoneapi.tools.Pair;
import net.redstonecraft.redstoneapi.tools.StringUtils;
import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;
import net.redstonecraft.redstonecloud.manager.commands.Command;
import net.redstonecraft.redstonecloud.manager.plugins.RedstonecloudPlugin;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * @author Redstonecrafter0
 */
public class CommandManager implements Completer {

    private final Map<String, Pair<Command, RedstonecloudPlugin>> commands = new TreeMap<>();
    private boolean internalCommandsSupplied = false;

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        String[] orgArgs = StringUtils.parseArgs(parsedLine.line());
        if (orgArgs.length > 0) {
            Command command = commands.get(orgArgs[0].toLowerCase()).getFirst();
            if (command != null) {
                String[] args = Arrays.copyOfRange(orgArgs, 1, orgArgs.length);
                try {
                    for (String i : command.onComplete(args)) {
                        list.add(new Candidate(i));
                    }
                } catch (Throwable ignored) {
                }
            } else if (!parsedLine.line().contains(" ")) {
                commands.keySet().stream().filter(i -> i.startsWith(parsedLine.line())).forEach(i -> list.add(new Candidate(i)));
            }
        }
    }

    public void registerCommands(RedstonecloudPlugin plugin, Command... commands) {
        if (internalCommandsSupplied && plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        for (Command i : commands) {
            if (!this.commands.containsKey(i.commandName)) {
                this.commands.put(i.commandName, new Pair<>(i, plugin));
            }
        }
    }

    public void performCommand(String input) {
        String[] orgArgs = StringUtils.parseArgs(input);
        Command command = commands.get(orgArgs[0].toLowerCase()).getFirst();
        if (command != null) {
            try {
                String[] args = Arrays.copyOfRange(orgArgs, 1, orgArgs.length);
                command.onCommand(args);
            } catch (Throwable e) {
                RedstonecloudManager.LOGGER.log(Level.SEVERE, "Error in command " + orgArgs[0].toLowerCase(), e);
            }
        } else {
            System.out.println("Command " + orgArgs[0].toLowerCase() + " not found.");
        }
    }

}
