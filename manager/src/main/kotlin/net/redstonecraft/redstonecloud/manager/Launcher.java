package net.redstonecraft.redstonecloud.manager;

import net.redstonecraft.redstoneapi.json.parser.JSONParser;
import net.redstonecraft.redstonecloud.manager.utils.Configuration;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * @author Redstonecrafter0
 */
public class Launcher {

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
    private static final SimpleDateFormat simpleDateFormatParse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Throwable {
        simpleDateFormat.setLenient(true);
        File logsFolder = new File("logs");
        if (!logsFolder.exists() || !logsFolder.isDirectory()) {
            logsFolder.mkdirs();
        }
        File latestLog = new File(logsFolder, "latest.log");
        if (latestLog.exists() && latestLog.isFile()) {
            try {
                FileInputStream is = new FileInputStream(latestLog);
                is.read();
                StringBuilder sb = new StringBuilder();
                int c;
                for (int i = 0; i < 19; i++) {
                    if ((c = is.read()) != -1) {
                        sb.append((char) c);
                    } else {
                        is.close();
                        throw new EOFException();
                    }
                }
                is.close();
                try {
                    Date date = simpleDateFormatParse.parse(sb.toString());
                    File file = new File(logsFolder, simpleDateFormat.format(date) + ".log.gz");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        try (GZIPOutputStream gzip = new GZIPOutputStream(fos)) {
                            is = new FileInputStream(latestLog);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                gzip.write(buffer, 0, len);
                            }
                            is.close();
                            gzip.finish();
                        }
                    }
                } catch (ParseException ignored) {
                }
            } catch (Throwable ignored) {
                latestLog.delete();
                latestLog.createNewFile();
            }
        }
        RedstonecloudManager.LOGGER.info("Starting...");
        File configFile;
        switch (args.length) {
            case 0:
                configFile = new File("config.json");
                break;
            case 1:
                configFile = new File(args[0]);
                break;
            default:
                return;
        }
        if (!configFile.exists()) {
            //noinspection ConstantConditions
            Files.copy(RedstonecloudManager.class.getResourceAsStream("/config.json"), configFile.toPath());
            RedstonecloudManager.LOGGER.info("Config was created. Go and modify it.");
            RedstonecloudManager.LOGGER.info("Exiting...");
            return;
        }
        Configuration config = new Configuration(JSONParser.parseObject(new String(new FileInputStream(configFile).readAllBytes(), StandardCharsets.UTF_8)));
        new RedstonecloudManager(config);
    }

}
