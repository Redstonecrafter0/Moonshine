package net.redstonecraft.redstoneclouddcbot;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.redstonecraft.redstoneapi.discord.DiscordBot;
import net.redstonecraft.redstoneapi.discord.abs.DiscordEvent;
import net.redstonecraft.redstoneapi.discord.abs.DiscordEventListener;
import net.redstonecraft.redstoneapi.tools.StringUtils;
import net.redstonecraft.redstonecloud.manager.RedstonecloudManager;
import net.redstonecraft.redstonecloud.manager.plugins.RedstonecloudPlugin;
import net.redstonecraft.redstoneclouddcbot.utils.LoggerOutputStream;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * @author Redstonecrafter0
 */
public class RedstonecloudDiscordBot extends RedstonecloudPlugin implements DiscordEventListener {

    private DiscordBot<?, ?> bot;
    private StreamHandler streamHandler;

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        streamHandler = new StreamHandler(new LoggerOutputStream(msg -> Objects.requireNonNull(Objects.requireNonNull(bot.getJda().getGuildById(getConfig().getString("guild"))).getTextChannelById(getConfig().getString("channel"))).sendMessage(msg).queue()), new Formatter() {
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
        });
        try {
            bot = new DiscordBot<>(getConfig().getString("token"), null);
            bot.getEventManager().addEventListener(this);
            RedstonecloudManager.LOGGER.addHandler(streamHandler);
        } catch (LoginException ignored) {
        }
    }

    @Override
    public void onDisable() {
        bot.getJda().shutdown();
        if (Arrays.asList(RedstonecloudManager.LOGGER.getHandlers()).contains(streamHandler)) {
            RedstonecloudManager.LOGGER.removeHandler(streamHandler);
        }
        bot = null;
    }

    @DiscordEvent
    public void onMessage(GuildMessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(getConfig().getString("prefix")) && event.getGuild().getId().equals(getConfig().getString("guild")) && event.getChannel().getId().equals(getConfig().getString("channel")) && Objects.requireNonNull(event.getMember()).hasPermission(Permission.valueOf(getConfig().getString("permission")))) {
            RedstonecloudManager.getInstance().dispatchCommand(event.getMessage().getContentRaw().substring(getConfig().getString("prefix").length()));
        }
    }

}
