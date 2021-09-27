package net.redstonecraft.redstonecloud.manager.commands;

import java.util.ArrayList;

/**
 * @author Redstonecrafter0
 */
public abstract class Command {

    public final String commandName;

    public Command(String commandName) {
        this.commandName = commandName;
    }

    public abstract void onCommand(String[] args);

    public Iterable<String> onComplete(String[] args) {
        return new ArrayList<>();
    }

}
