package com.conquest.hud.client;

import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;

public class ConquestHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null) return;

            // Получаем синхронизированные с сервера статы
            IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
            TextRenderer renderer = client.textRenderer;

            // ... (получение stats и renderer)
            int x = 10;
            int y = 10;
            int color = 0xFFFFFF;

            drawContext.drawText(renderer, "Strength: " + stats.getStrength(), x, y, color, true);
            drawContext.drawText(renderer, "Agility: " + stats.getAgility(), x, y + 15, color, true);
            drawContext.drawText(renderer, "Vitality: " + stats.getVitality(), x, y + 30, color, true);
            drawContext.drawText(renderer, "Metabolism: " + stats.getMetabolism(), x, y + 45, color, true);
            drawContext.drawText(renderer, "Intellect: " + stats.getIntellect(), x, y + 60, color, true);

            float currentWeight = WeightManager.getCurrentWeight(player);
            float maxWeight = WeightManager.getMaxWeight(player);
            drawContext.drawText(renderer, String.format("Weight: %.1f / %.1f kg", currentWeight, maxWeight), x, y + 80, 0xFFAA00, true);
        });
    }
}