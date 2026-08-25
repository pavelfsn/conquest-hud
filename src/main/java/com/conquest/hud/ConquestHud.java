package com.conquest.hud;

import com.conquest.hud.core.logger.ModLogger;
import net.fabricmc.api.ModInitializer;

public class ConquestHud implements ModInitializer {
    public static final String MOD_ID = "conquest-hud";

    @Override
    public void onInitialize() {
        // Инициализация системы логирования
        ModLogger.init();
        ModLogger.info("CORE", "Conquest HUD Core Module initializing...");
    }
}