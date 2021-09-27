package net.redstonecraft.redstoneclouddcbot.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author Redstonecrafter0
 */
public class LoggerOutputStream extends OutputStream {

    private StringBuilder sb = new StringBuilder();
    private final Consumer<String> messageHandler;

    public LoggerOutputStream(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void write(int b) throws IOException {
        if (new String(new byte[]{(byte) b}, StandardCharsets.UTF_8).equals("\n")) {
            messageHandler.accept(sb.toString());
            sb = new StringBuilder();
        } else {
            sb.append(new String(new byte[]{(byte) b}));
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        messageHandler.accept(new String(b, StandardCharsets.UTF_8));
    }

}
