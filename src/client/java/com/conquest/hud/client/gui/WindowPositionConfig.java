package com.conquest.hud.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WindowPositionConfig {
    // Путь к файлу: .minecraft/config/conquest_windows.json
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("conquest_windows.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, int[]> positions = new HashMap<>();

    static {
        load(); // Автоматически загружаем файл при запуске игры
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                positions = GSON.fromJson(reader, new TypeToken<Map<String, int[]>>(){}.getType());
                if (positions == null) positions = new HashMap<>();
            } catch (Exception e) {
                System.err.println("[Conquest] Ошибка загрузки позиций окон: " + e.getMessage());
            }
        }
    }

    public static void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(positions, writer);
        } catch (Exception e) {
            System.err.println("[Conquest] Ошибка сохранения позиций окон: " + e.getMessage());
        }
    }

    public static int[] get(String title) {
        return positions.getOrDefault(title, new int[]{-1, -1});
    }

    public static void set(String title, int x, int y) {
        positions.put(title, new int[]{x, y});
        save(); // Сразу физически сохраняем файл при отпускании окна мышью
    }
}