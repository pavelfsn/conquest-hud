package com.conquest.hud.client.render;

import com.conquest.hud.client.gui.WindowPositionConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public class BuffsHudRenderer implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        if (effects.isEmpty()) return;

        // Получаем чистые координаты
        int[] pos = WindowPositionConfig.get("Бафы");
        int startX = pos[0];
        int startY = pos[1];

        StatusEffectSpriteManager spriteManager = client.getStatusEffectSpriteManager();
        int i = 0;
        int size = 32;
        int gap = 2;

        for (StatusEffectInstance effect : effects) {
            int x = startX + (i * (size + gap));

            context.fill(x, startY, x + size, startY + size, 0x99000000);
            context.drawBorder(x, startY, size, size, 0xFF555555);

            Sprite sprite = spriteManager.getSprite(effect.getEffectType());
            if (sprite != null) {
                RenderSystem.setShaderTexture(0, sprite.getAtlasId());
                context.drawSprite(x + 8, startY + 8, 0, 16, 16, sprite);
            }

            int duration = effect.getDuration() / 20;
            String time = String.format("%d:%02d", duration / 60, duration % 60);
            context.drawTextWithShadow(client.textRenderer, time, x + 2, startY + size + 2, 0xFFFFFF);

            i++;
        }
    }
}