package com.conquest.hud.client.render;

import com.conquest.hud.client.ConquestKeybinds;
import com.conquest.hud.client.gui.ConquestInventoryScreen;
import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import com.conquest.hud.client.gui.ModularWindow;

public class CustomHotbarRenderer implements HudRenderCallback {
    private final ModularWindow renderHelper = new ModularWindow("helper", 0, 0, 0, 0, false);

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        if (client.currentScreen instanceof ConquestInventoryScreen) return;

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);
        if (containers == null) return;

        IContainer hotbar = containers.getHotbar();
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int slotSize = 32;
        int gap = 2;
        int totalWidth = (9 * slotSize) + (8 * gap);
        int startX = screenWidth - totalWidth - 15;
        int startY = screenHeight - slotSize - 15;

        for (int i = 0; i < 9; i++) {
            int x = startX + i * (slotSize + gap);

            context.fill(x, startY, x + slotSize, startY + slotSize, 0x99000000);
            context.drawBorder(x, startY, slotSize, slotSize, 0xFF555555);

            ItemStack stack = hotbar.getStack(i);
            if (!stack.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(x, startY, 0);
                context.getMatrices().scale(2.0f, 2.0f, 1.0f);
                context.drawItem(stack, 0, 0);
                renderHelper.drawCustomItemOverlay(context, client.textRenderer, stack, false);
                context.getMatrices().pop();
            }

            String keyName = ConquestKeybinds.actionKeys[i].getBoundKeyLocalizedText().getString();

            context.getMatrices().push();
            // Слой текста поднят до Z=250, чтобы предметы его не перекрывали
            context.getMatrices().translate(x + 2, startY + slotSize - 10, 250);
            if (client.textRenderer.getWidth(keyName) > 12) {
                context.getMatrices().scale(0.6f, 0.6f, 1.0f);
            }
            context.drawTextWithShadow(client.textRenderer, keyName, 0, 0, 0xFFFFFF);
            context.getMatrices().pop();
        }
    }
}