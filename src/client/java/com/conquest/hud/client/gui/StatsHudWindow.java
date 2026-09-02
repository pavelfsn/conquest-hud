package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.PlayerStatsRenderer;
import net.minecraft.client.gui.DrawContext;

public class StatsHudWindow extends ModularWindow {

    public StatsHudWindow(String title, int defaultX, int defaultY) {
        super(title, defaultX, defaultY, 296, 25, true);
    }

    @Override
    public void renderNanoVGBackground(int mouseX, int mouseY) {
        if (!isVisible()) return;
        if (isDragging) {
            this.x = mouseX - dragOffsetX;
            this.y = mouseY - dragOffsetY;
        }
        // Блокируем отрисовку шапки и фона (оставляем виджет чистым)
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        // Легкая подсветка при наведении, чтобы игрок понимал, за что тянуть
        if (isHovered(mouseX, mouseY)) {
            context.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x22FFFFFF);
        }

        PlayerStatsRenderer.render(context, x, y, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        // Позволяем перетаскивать за любую часть бара, а не только за шапку
        if (button == 0 && isHovered(mouseX, mouseY)) {
            isDragging = true;
            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;
            return true;
        }
        return false;
    }
}