package com.conquest.hud.core.logger;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModLogger {
    public static final String MOD_ID = "conquest-hud";
    private static final Logger CONSOLE_LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static BufferedWriter fileWriter;

    public static void init() {
        // Получаем корневую папку игры/сервера
        File logDir = new File(FabricLoader.getInstance().getGameDir().toFile(), "logs/conquest_hud");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        File logFile = new File(logDir, "latest.log");
        try {
            fileWriter = new BufferedWriter(new FileWriter(logFile, true));
            info("NET", "Logger initialized successfully.");
        } catch (IOException e) {
            CONSOLE_LOGGER.error("Failed to initialize custom file logger", e);
        }
    }

    public static void info(String channel, String message) {
        log("INFO", channel, message);
    }

    public static void warn(String channel, String message) {
        log("WARN", channel, message);
    }

    public static void error(String channel, String message) {
        log("ERROR", channel, message);
    }

    private static void log(String level, String channel, String message) {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String formattedMessage = String.format("[%s] [%s] [%s]: %s", time, level, channel, message);

        // Вывод в стандартную консоль
        switch (level) {
            case "INFO" -> CONSOLE_LOGGER.info("[{}] {}", channel, message);
            case "WARN" -> CONSOLE_LOGGER.warn("[{}] {}", channel, message);
            case "ERROR" -> CONSOLE_LOGGER.error("[{}] {}", channel, message);
        }

        // Асинхронная запись в файл
        if (fileWriter != null) {
            EXECUTOR.submit(() -> {
                try {
                    fileWriter.write(formattedMessage);
                    fileWriter.newLine();
                    fileWriter.flush();
                } catch (IOException e) {
                    CONSOLE_LOGGER.error("Failed to write to file", e);
                }
            });
        }
    }
}