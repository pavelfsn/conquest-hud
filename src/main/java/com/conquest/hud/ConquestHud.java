package com.conquest.hud;

import com.conquest.hud.core.inventory.ConquestScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import com.conquest.hud.core.config.ItemWeightConfig;

public class ConquestHud implements ModInitializer {
    public static final Identifier OPEN_UI_PACKET = new Identifier("conquest", "open_ui");

    @Override
    public void onInitialize() {
        // Регистрация ScreenHandler
        ConquestScreenHandler.TYPE = Registry.register(
                Registries.SCREEN_HANDLER,
                new Identifier("conquest", "custom_inventory"),
                new ScreenHandlerType<>(ConquestScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

        ItemWeightConfig.load();
        // Обработка пакета открытия UI от клиента
        ServerPlayNetworking.registerGlobalReceiver(OPEN_UI_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                        (syncId, inventory, p) -> new ConquestScreenHandler(syncId, inventory),
                        Text.literal("Conquest UI")
                ));
            });
        });
    }
}