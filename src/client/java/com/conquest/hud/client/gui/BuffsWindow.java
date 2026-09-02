package com.conquest.hud.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Collection;

public class BuffsWindow extends ModularWindow {

    public BuffsWindow(String title, int defaultX, int defaultY) {
        super(title, defaultX, defaultY, 32, 32, true);
    }

    @Override
    public void renderNanoVGBackground(int mouseX, int mouseY) {
        if (!isVisible()) return;

        if (isDragging) {
            this.x = mouseX - dragOffsetX;
            this.y = mouseY - dragOffsetY;
        }
        // Переопределяем метод, чтобы ничего не рисовать (ни фона, ни шапки)
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!isVisible()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) return;

        Collection<StatusEffectInstance> effects = client.player.getStatusEffects();
        int count = effects.size();

        // Динамически расширяем зону захвата мыши в зависимости от количества бафов
        if (count > 0) {
            this.width = count * 34; // 32px слот + 2px отступ
            this.height = 32;

            // Легкая подсветка зоны при наведении, чтобы игрок понимал, что её можно тащить
            if (isHovered(mouseX, mouseY)) {
                context.fill(x - 2, y - 2, x + width, y + height + 12, 0x22FFFFFF);
            }
        } else {
            // Если бафов нет, рисуем квадрат-заглушку, чтобы в режиме редактора (открытом инвентаре) было за что схватиться
            this.width = 32;
            this.height = 32;
            context.fill(x, y, x + 32, y + 32, 0x44FFFFFF);
            context.drawBorder(x, y, 32, 32, 0x88FFFFFF);
            context.drawTextWithShadow(client.textRenderer, "Бафы", x + 4, y + 12, 0x88FFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        // Перетаскиваем виджет за клик в ЛЮБУЮ точку его зоны, а не только за шапку
        if (button == 0 && isHovered(mouseX, mouseY)) {
            isDragging = true;
            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;
            return true;
        }
        return false;
    }
}