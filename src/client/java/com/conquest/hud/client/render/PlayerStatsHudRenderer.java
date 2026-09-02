package com.conquest.hud.client.render;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import com.conquest.hud.client.gui.WindowPositionConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class PlayerStatsHudRenderer implements HudRenderCallback {
    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options.hudHidden) return;

        // Отключаем рендер обычного HUD, если открыт инвентарь (там будет рендериться окно-призрак)
        if (client.currentScreen instanceof ConquestInventoryScreen) return;

        int[] pos = WindowPositionConfig.get("HUD_Статусы");
        int sh = context.getScaledWindowHeight();

        // Позиция по умолчанию: Слева снизу (x = 20, y = sh - 40)
        int x = (pos[0] >= 0) ? pos[0] : 20;
        int y = (pos[1] >= 0) ? pos[1] : 20;

        PlayerStatsRenderer.render(context, x, y, tickDelta);
    }
}