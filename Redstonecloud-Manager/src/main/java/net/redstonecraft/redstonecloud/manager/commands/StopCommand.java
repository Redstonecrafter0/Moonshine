package net.redstonecraft.redstonecloud.manager.commands;

import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;

/**
 * @author Redstonecrafter0
 */
public class StopCommand extends Command {

    public StopCommand() {
        super("stop");
    }

    @Override
    public void onCommand(String[] args) {
        if (args.length == 0) {
            RedstonecloudManager.getInstance().shutdown();
        } else {
            RedstonecloudManager.LOGGER.info("This command does not have args.");
        }
    }

}
