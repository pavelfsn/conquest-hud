package com.conquest.hud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class LevelUpHudRenderer implements HudRenderCallback {
    private static int tickCounter = -1;
    private static int oldLevel = 0;
    private static int newLevel = 0;

    public static void trigger(int oldLvl, int newLvl) {
        oldLevel = oldLvl;
        newLevel = newLvl;
        tickCounter = 0;
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        if (tickCounter < 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int sw = context.getScaledWindowWidth();

        tickCounter++;
        if (tickCounter > 160) { // Анимация длится 8 секунд (160 тиков)
            tickCounter = -1;
            return;
        }

        float alpha = 1.0f;
        if (tickCounter < 20) alpha = tickCounter / 20.0f;
        else if (tickCounter > 120) alpha = (160 - tickCounter) / 40.0f;

        int alphaInt = (int) (alpha * 255);
        int bgColorTop = (Math.min(alphaInt, 150) << 24) | 0x885500; // Золотистый полупрозрачный
        int bgColorBot = 0x00885500; // Прозрачный низ градиента

        RenderSystem.enableBlend();
        context.fillGradient(0, 0, sw, 80, bgColorTop, bgColorBot);

        int titleColor = (alphaInt << 24) | 0xFFD700; // Золотой
        int textColor = (alphaInt << 24) | 0xFFFFFF;  // Белый

        String title = "УРОВЕНЬ ПОВЫШЕН";
        String text = "Ур. " + oldLevel + " -> Ур. " + newLevel;

        context.getMatrices().push();
        context.getMatrices().translate(sw / 2f, 20, 0);
        context.getMatrices().scale(1.5f, 1.5f, 1.0f);
        context.drawTextWithShadow(client.textRenderer, title, -client.textRenderer.getWidth(title) / 2, 0, titleColor);
        context.getMatrices().pop();

        context.getMatrices().push();
        context.getMatrices().translate(sw / 2f, 45, 0);
        context.drawTextWithShadow(client.textRenderer, text, -client.textRenderer.getWidth(text) / 2, 0, textColor);
        context.getMatrices().pop();

        RenderSystem.disableBlend();
    }
}