package com.conquest.hud.client;

import com.conquest.hud.client.gui.WindowPositionConfig;
import com.conquest.hud.client.render.NanoVGHelper;
import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import com.conquest.hud.core.inventory.ConquestScreenHandler;
import com.conquest.hud.client.gui.ConquestInventoryScreen;

public class ConquestHudClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ConquestKeybinds.register();
        WindowPositionConfig.load();
        HandledScreens.register(ConquestScreenHandler.TYPE, ConquestInventoryScreen::new);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null) return;

            IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
            IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.get(player);
            TextRenderer renderer = client.textRenderer;

            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            // Инициализация кадра NanoVG
            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            nvg.beginFrame(screenWidth, screenHeight);

            // 1. Отрисовка графической шкалы Здоровья (квадратная)
            float hpPercent = player.getHealth() / player.getMaxHealth();
            nvg.drawRoundedRect(10, 10, 200, 15, 0.0f, 0x222222, 0.8f); // Фон
            nvg.drawRoundedRect(10, 10, 200 * hpPercent, 15, 0.0f, 0xFF3333, 1.0f); // Заливка

            // 2. Отрисовка графической шкалы Стамины (квадратная, тонкая, белая)
            float staminaPercent = staminaComp.getStamina() / staminaComp.getMaxStamina();
            nvg.drawRoundedRect(10, 28, 150, 4, 0.0f, 0x222222, 0.8f); // Фон
            nvg.drawRoundedRect(10, 28, 150 * staminaPercent, 4, 0.0f, 0xFFFFFF, 1.0f); // Заливка

            // 3. Фоны слотов хотбара (Увеличены в 2.5 раза)
            // 3. Фоны слотов хотбара (Масштаб 2.0)
            int slotSize = 44; // Было 55
            int hotbarStartX = screenWidth - (9 * slotSize) - 10;
            int hotbarStartY = screenHeight - slotSize - 10;
            int selectedSlot = player.getInventory().selectedSlot;

            for (int i = 0; i < 9; i++) {
                int slotX = hotbarStartX + i * slotSize;
                nvg.drawRoundedRect(slotX, hotbarStartY, 40, 40, 0.0f, 0x222222, 0.6f); // Фон 40x40

                if (i == selectedSlot) {
                    nvg.drawRoundedRect(slotX, hotbarStartY, 40, 4, 0.0f, 0xFFAA00, 1.0f); // Индикатор 40x4
                }
            }

            // Завершение кадра NanoVG
            nvg.endFrame();

            // --------------------------------------------------------
            // Ванильный рендер поверх графики (Текст и предметы)
            // --------------------------------------------------------

            // Текст ХП (Центрирование)
            String hpText = String.format("%.0f / %.0f", player.getHealth(), player.getMaxHealth());
            int textWidth = renderer.getWidth(hpText);
            int textX = 10 + (200 / 2) - (textWidth / 2);
            int textY = 10 + (15 / 2) - (renderer.fontHeight / 2) + 1;
            drawContext.drawText(renderer, hpText, textX, textY, 0xFFFFFF, true);

            // Отрисовка предметов в кастомном хотбаре (Исправленное центрирование)
            for (int i = 0; i < 9; i++) {
                int slotX = hotbarStartX + i * slotSize;
                net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);

                if (!stack.isEmpty()) {
                    drawContext.getMatrices().push();

                    // 1. Смещаем точку координат ровно в центр фона (фон 40x40, центр = 20)
                    drawContext.getMatrices().translate(slotX + 20, hotbarStartY + 20, 0);
                    // 2. Увеличиваем масштаб
                    drawContext.getMatrices().scale(2.0f, 2.0f, 1.0f);

                    // 3. Отрисовываем предмет со смещением -8 (половина ванильного размера 16x16),
                    // чтобы его геометрический центр совпал с нулем матрицы.
                    drawContext.drawItem(stack, -8, -8);
                    drawContext.drawItemInSlot(renderer, stack, -8, -8);

                    drawContext.getMatrices().pop();
                }
            }

            // Правый верхний угол: Текст статов
            int rightX = screenWidth - 120;
            drawContext.drawText(renderer, "Strength: " + stats.getStrength(), rightX, 10, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Agility: " + stats.getAgility(), rightX, 25, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Vitality: " + stats.getVitality(), rightX, 40, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Metabolism: " + stats.getMetabolism(), rightX, 55, 0xFFFFFF, true);
            drawContext.drawText(renderer, "Intellect: " + stats.getIntellect(), rightX, 70, 0xFFFFFF, true);

            float currentWeight = WeightManager.getCurrentWeight(player);
            float maxWeight = WeightManager.getMaxWeight(player);
            drawContext.drawText(renderer, String.format("Weight: %.1f / %.1f kg", currentWeight, maxWeight), rightX, 90, 0xFFAA00, true);
        });
    }
}