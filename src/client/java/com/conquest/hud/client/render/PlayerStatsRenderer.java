package com.conquest.hud.client.render;

import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

public class PlayerStatsRenderer {
    private static float currentHp = 200f;
    private static float trailHp = 200f;
    private static long hpTimer = 0L;

    private static float currentStamina = 100f;
    private static float trailStamina = 100f;
    private static long stamTimer = 0L;

    private static boolean firstRender = true;

    public static void render(DrawContext context, int x, int y, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;
        if (player == null) return;

        float targetHp = player.getHealth();
        float maxHp = player.getMaxHealth();

        float targetStamina = 0f;
        float maxStamina = 100f;
        IStaminaComponent stam = StatsComponentRegistry.STAMINA.getNullable(player);
        if (stam != null) {
            targetStamina = stam.getStamina();
            maxStamina = stam.getMaxStamina();
        }

        if (firstRender) {
            currentHp = targetHp;
            trailHp = targetHp;
            currentStamina = targetStamina;
            trailStamina = targetStamina;
            firstRender = false;
        }

        long currentTime = System.currentTimeMillis();

        // ЛОГИКА ХП
        if (targetHp < currentHp) {
            currentHp = targetHp;
            hpTimer = currentTime + 800L;
        } else if (targetHp > currentHp) {
            currentHp = MathHelper.lerp(0.1f, currentHp, targetHp);
            trailHp = currentHp;
            if (Math.abs(currentHp - targetHp) < 0.1f) currentHp = targetHp;
        }

        if (trailHp > currentHp) {
            if (currentTime > hpTimer) trailHp = MathHelper.lerp(0.1f, trailHp, currentHp);
        } else {
            trailHp = currentHp;
        }

        // ЛОГИКА СТАМИНЫ
        if (targetStamina < currentStamina) {
            if (currentStamina - targetStamina > 2.0f) {
                currentStamina = targetStamina;
                stamTimer = currentTime + 500L;
            } else {
                currentStamina = MathHelper.lerp(0.2f, currentStamina, targetStamina);
                trailStamina = currentStamina;
            }
        } else if (targetStamina > currentStamina) {
            currentStamina = MathHelper.lerp(0.1f, currentStamina, targetStamina);
            trailStamina = currentStamina;
        }

        if (trailStamina > currentStamina) {
            if (currentTime > stamTimer) trailStamina = MathHelper.lerp(0.1f, trailStamina, currentStamina);
        } else {
            trailStamina = currentStamina;
        }

        // ОТРИСОВКА
        int barWidth = 296;

        float hpRatio = Math.max(0, Math.min(1.0f, currentHp / maxHp));
        float trailHpRatio = Math.max(0, Math.min(1.0f, trailHp / maxHp));

        context.fill(x, y, x + barWidth, y + 18, 0x88330000);
        if (trailHpRatio > hpRatio) context.fill(x, y, x + (int)(barWidth * trailHpRatio), y + 18, 0xFFFFFFFF);
        context.fill(x, y, x + (int)(barWidth * hpRatio), y + 18, 0xFF8D0000);

        String hpText = String.format("%d / %d", (int)Math.ceil(currentHp), (int)maxHp);
        context.drawTextWithShadow(client.textRenderer, hpText, x + (barWidth/2) - (client.textRenderer.getWidth(hpText)/2), y + 5, 0xFFFFFF);

        float stamRatio = Math.max(0, Math.min(1.0f, currentStamina / maxStamina));
        float trailStamRatio = Math.max(0, Math.min(1.0f, trailStamina / maxStamina));

        context.fill(x, y + 21, x + barWidth, y + 25, 0x88222222);
        if (trailStamRatio > stamRatio) context.fill(x, y + 21, x + (int)(barWidth * trailStamRatio), y + 25, 0xFFFFFFFF);
        context.fill(x, y + 21, x + (int)(barWidth * stamRatio), y + 25, 0xFFADADAD);
    }
}