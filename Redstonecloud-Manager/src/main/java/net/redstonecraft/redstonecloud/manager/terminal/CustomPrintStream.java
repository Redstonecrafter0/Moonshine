package net.redstonecraft.redstonecloud.manager.terminal;

import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Redstonecrafter0
 */
public class CustomPrintStream extends PrintStream {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final LineReader lineReader;

    public CustomPrintStream(PrintStream out, LineReader lineReader) {
        super(out);
        this.lineReader = lineReader;
    }

    @Override
    public void println(String x) {
        lineReader.printAbove("[" + simpleDateFormat.format(new Date()) + "] " + x + "\n");
    }

    @Override
    public void print(String s) {
        lineReader.printAbove("[" + simpleDateFormat.format(new Date()) + "] " + s);
    }

}
