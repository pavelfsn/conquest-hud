package com.conquest.hud.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ItemWeightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("conquest_weights.json").toFile();
    private static Map<String, Float> weights = new HashMap<>();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                Type type = new TypeToken<Map<String, Float>>(){}.getType();
                weights = GSON.fromJson(reader, type);
                if (weights == null) weights = new HashMap<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            generateDefault();
        }
    }

    private static void generateDefault() {
        weights.put("minecraft:apple", 0.2f);
        weights.put("minecraft:iron_sword", 2.5f);
        weights.put("minecraft:dirt", 1.0f);
        weights.put("minecraft:stone", 1.5f);

        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(weights, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static float getWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0.0f;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        // Если предмета нет в конфиге, выдаем дефолтный вес 0.1 кг
        return weights.getOrDefault(id.toString(), 0.1f);
    }
}