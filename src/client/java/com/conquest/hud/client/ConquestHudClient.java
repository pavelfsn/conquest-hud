package com.conquest.hud.client;

import com.conquest.hud.client.network.ModClientNetworking;
import com.conquest.hud.client.render.BuffsHudRenderer;
import com.conquest.hud.client.render.CustomHotbarRenderer;
import com.conquest.hud.client.render.LevelUpHudRenderer;
import com.conquest.hud.client.render.PlayerStatsHudRenderer;
import com.conquest.hud.client.render.nanovg.NanoVGManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;


public class ConquestHudClient implements ClientModInitializer {
    private static boolean nanoVgInitialized = false;

    @Override
    public void onInitializeClient() {
        ModClientNetworking.registerClientReceivers();
        ConquestKeybinds.register();
        // Внутри метода инициализации клиента:

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (!nanoVgInitialized) {
                NanoVGManager.init();
                nanoVgInitialized = true;
            }
        });

        HudRenderCallback.EVENT.register(new CustomHotbarRenderer());
        HudRenderCallback.EVENT.register(new BuffsHudRenderer()); // Активация рендера бафов
        HudRenderCallback.EVENT.register(new LevelUpHudRenderer());
        HudRenderCallback.EVENT.register(new PlayerStatsHudRenderer());
    }

    public static boolean isNanoVGReady() {
        return nanoVgInitialized;
    }
}