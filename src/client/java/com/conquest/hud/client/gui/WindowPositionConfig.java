package com.conquest.hud.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WindowPositionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("conquest_ui_pos.json").toFile();
    private static Map<String, int[]> positions = new HashMap<>();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                Type type = new TypeToken<Map<String, int[]>>(){}.getType();
                positions = GSON.fromJson(reader, type);
                if (positions == null) positions = new HashMap<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        positions.putIfAbsent("equipment", new int[]{400, 100});
        positions.putIfAbsent("inventory", new int[]{600, 100});
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(positions, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int[] get(String window) {
        int[] pos = positions.get(window);
        if (pos == null) {
            pos = new int[]{100, 100};
            positions.put(window, pos);
        }
        return pos;
    }

    public static void set(String window, int x, int y) {
        positions.put(window, new int[]{x, y});
        save(); // сохраняем сразу
    }
}