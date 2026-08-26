package com.conquest.hud.client;

import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.IStaminaComponent;
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

            IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
            IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.get(player);
            TextRenderer renderer = client.textRenderer;

            int screenWidth = client.getWindow().getScaledWidth();

            // 1. Левый верхний угол: Здоровье и Стамина
            float currentHp = player.getHealth();
            float maxHp = player.getMaxHealth();
            drawContext.drawText(renderer, String.format("HP: %.1f / %.1f", currentHp, maxHp), 10, 10, 0xFF5555, true);

            float stamina = staminaComp.getStamina();
            float maxStamina = staminaComp.getMaxStamina();
            drawContext.drawText(renderer, String.format("STAMINA: %.0f / %.0f", stamina, maxStamina), 10, 25, 0x55FF55, true);

            // 2. Правый верхний угол: RPG Характеристики
            int rightX = screenWidth - 120;
            drawContext.drawText(renderer, "Strength: " + stats.getStrength(), rightX, 10, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Agility: " + stats.getAgility(), rightX, 25, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Vitality: " + stats.getVitality(), rightX, 40, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Metabolism: " + stats.getMetabolism(), rightX, 55, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Intellect: " + stats.getIntellect(), rightX, 70, 0xFFFFFF, true);

            // 3. Вывод переносимого веса под статами
            float currentWeight = WeightManager.getCurrentWeight(player);
            float maxWeight = WeightManager.getMaxWeight(player);
            drawContext.drawText(renderer, String.format("Weight: %.1f / %.1f kg", currentWeight, maxWeight), rightX, 90, 0xFFAA00, true);
        });
    }
}