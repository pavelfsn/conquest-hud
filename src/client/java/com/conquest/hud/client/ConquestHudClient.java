package com.conquest.hud.client;

import com.conquest.hud.client.gui.WindowPositionConfig;
import com.conquest.hud.client.render.NanoVGHelper;
import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.entity.player.PlayerEntity;
import com.conquest.hud.core.inventory.ConquestScreenHandler;
import com.conquest.hud.client.gui.ConquestInventoryScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConquestHudClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ConquestHUD");
    private static boolean nanoVGReady = false;

    @Override
    public void onInitializeClient() {
        ConquestKeybinds.register();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                NanoVGHelper.INSTANCE.init();
                nanoVGReady = true;
                LOGGER.info("NanoVG initialized successfully");
            } catch (Exception e) {
                LOGGER.error("Failed to initialize NanoVG, custom HUD disabled", e);
                nanoVGReady = false;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            NanoVGHelper.INSTANCE.cleanup();
            nanoVGReady = false;
        });

        // Тикер для прогресс-бара действий
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientActionManager.tick();
        });

        WindowPositionConfig.load();
        HandledScreens.register(ConquestScreenHandler.TYPE, ConquestInventoryScreen::new);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.registerGlobalReceiver(com.conquest.hud.ConquestPackets.SYNC_ACTION_PACKET, (clientNetwork, handler, buf, responseSender) -> {
            String text = buf.readString();
            int ticks = buf.readInt();
            clientNetwork.execute(() -> {
                if (ticks > 0) {
                    ClientActionManager.startAction(text, ticks);
                } else {
                    ClientActionManager.stopAction();
                }
            });
        });

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Проверяем нажатия ванильных клавиш 1, 2, 3 (первые 3 слота)
            for (int i = 0; i < 3; i++) {
                if (client.options.hotbarKeys[i].wasPressed()) {
                    net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                    buf.writeInt(i + 1); // 1, 2, 3
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(com.conquest.hud.ConquestPackets.CHANGE_WEAPON_PACKET, buf);
                }
            }

            // Поглощаем остальные нажатия (4-9), чтобы они ничего не делали
            for (int i = 3; i < 9; i++) {
                while (client.options.hotbarKeys[i].wasPressed()) {}
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null || !nanoVGReady) return;

            IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
            IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.get(player);
            if (stats == null || staminaComp == null) return;

            TextRenderer renderer = client.textRenderer;
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();

            NanoVGHelper nvg = NanoVGHelper.INSTANCE;
            if (!nvg.isInitialized()) return;

            nvg.beginFrame(screenWidth, screenHeight);

            if (ClientActionManager.isActive()) {
                float progress = ClientActionManager.getProgress();
                int barWidth = 160;
                int barHeight = 12;
                int barX = (screenWidth / 2) - (barWidth / 2);
                int barY = screenHeight - 120;

                nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 2.0f, 0x1A1A1A, 0.9f);
                nvg.drawRoundedRect(barX, barY, barWidth * progress, barHeight, 2.0f, 0xDDDDDD, 1.0f);

                nvg.drawRoundedRect(barX, barY, barWidth, 1, 0.0f, 0x333333, 1.0f);
                nvg.drawRoundedRect(barX, barY, 1, barHeight, 0.0f, 0x333333, 1.0f);
                nvg.drawRoundedRect(barX + barWidth - 1, barY, 1, barHeight, 0.0f, 0x333333, 1.0f);
                nvg.drawRoundedRect(barX, barY + barHeight - 1, barWidth, 1, 0.0f, 0x333333, 1.0f);
            }

            int slotSize = 44;
            int hotbarStartX = screenWidth - (9 * slotSize) - 10;
            int hotbarStartY = screenHeight - slotSize - 10;
            int selectedSlot = player.getInventory().selectedSlot;

            for (int i = 0; i < 9; i++) {
                int slotX = hotbarStartX + i * slotSize;
                nvg.drawRoundedRect(slotX, hotbarStartY, 40, 40, 0.0f, 0x222222, 0.6f);
                if (i == selectedSlot) {
                    nvg.drawRoundedRect(slotX, hotbarStartY, 40, 4, 0.0f, 0xFFAA00, 1.0f);
                }
            }

            float hpPercent = player.getHealth() / player.getMaxHealth();
            nvg.drawRoundedRect(10, 10, 200, 15, 0.0f, 0x222222, 0.8f);
            nvg.drawRoundedRect(10, 10, 200 * hpPercent, 15, 0.0f, 0xFF3333, 1.0f);

            float staminaPercent = staminaComp.getStamina() / staminaComp.getMaxStamina();
            nvg.drawRoundedRect(10, 28, 150, 4, 0.0f, 0x222222, 0.8f);
            nvg.drawRoundedRect(10, 28, 150 * staminaPercent, 4, 0.0f, 0xFFFFFF, 1.0f);

            nvg.endFrame();

            // Текст внутри прогресс-бара
            if (ClientActionManager.isActive()) {
                String text = ClientActionManager.getActionText();
                int textW = renderer.getWidth(text);
                int barX = (screenWidth / 2) - (160 / 2);
                int barY = screenHeight - 120;

                int textColor = (ClientActionManager.getProgress() > 0.5f) ? 0x111111 : 0xFFFFFF;
                // Y смещен для соответствия новой высоте (12px)
                drawContext.drawText(renderer, text, barX + (160 / 2) - (textW / 2), barY + 2, textColor, false);
            }

            String hpText = String.format("%.0f / %.0f", player.getHealth(), player.getMaxHealth());
            int textWidth = renderer.getWidth(hpText);
            drawContext.drawText(renderer, hpText, 10 + (200 / 2) - (textWidth / 2), 10 + (15 / 2) - (renderer.fontHeight / 2) + 1, 0xFFFFFF, true);

            for (int i = 0; i < 9; i++) {
                int slotX = hotbarStartX + i * slotSize;
                var stack = player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(slotX + 20, hotbarStartY + 20, 0);
                    drawContext.getMatrices().scale(2.0f, 2.0f, 1.0f);
                    drawContext.drawItem(stack, -8, -8);
                    drawContext.drawItemInSlot(renderer, stack, -8, -8);
                    drawContext.getMatrices().pop();
                }
            }

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

    public static boolean isNanoVGReady() {
        return nanoVGReady && NanoVGHelper.INSTANCE.isInitialized();
    }
}