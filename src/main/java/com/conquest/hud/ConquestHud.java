package com.conquest.hud;

import com.conquest.hud.core.command.RpgCommand;
import com.conquest.hud.core.inventory.ConquestScreenHandler;
import com.conquest.hud.core.inventory.EquipTaskManager;
import com.conquest.hud.core.logger.ModLogger;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import com.conquest.hud.core.config.ItemWeightConfig;

public class ConquestHud implements ModInitializer {
    public static final Identifier OPEN_UI_PACKET = new Identifier("conquest", "open_ui");

    @Override
    public void onInitialize() {
        ModLogger.init(); // Активация логгера

        ConquestScreenHandler.TYPE = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("conquest", "custom_inventory"),
                new ScreenHandlerType<>(ConquestScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        ItemWeightConfig.load();
        ConquestPackets.registerServerReceivers();
        RpgCommand.register(); // Активация команд

        // Регистрация серверного тикера для прогресс-бара
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EquipTaskManager.tickTasks(server.getPlayerManager().getPlayerList());
        });

        // Очистка задач при дисконнекте игрока (устранение утечки памяти)
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            EquipTaskManager.cancelTask(handler.player.getUuid());
        });
    }
}