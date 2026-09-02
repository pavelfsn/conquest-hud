package com.conquest.hud.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public class ContextMenuWidget {
    private int x, y;
    private boolean visible = false;
    private final int width = 86; // Уширено для текста
    private final int buttonHeight = 12;
    private List<ActionOption> options;

    public void open(int x, int y, List<ActionOption> options) {
        this.x = x;
        this.y = y;
        this.options = options;
        this.visible = true;
    }

    public void close() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        if (!visible || options == null) return;

        RenderSystem.disableDepthTest(); // Отключаем глубину, чтобы всегда рендерить поверх лута

        int height = options.size() * buttonHeight;
        context.fill(x, y, x + width, y + height, 0xEE222222);
        context.drawBorder(x, y, width, height, 0xFF555555);

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        for (int i = 0; i < options.size(); i++) {
            int btnY = y + (i * buttonHeight);
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= btnY && mouseY < btnY + buttonHeight;

            if (hovered) {
                context.fill(x + 1, btnY, x + width - 1, btnY + buttonHeight, 0x55FFFFFF);
            }
            context.drawTextWithShadow(tr, options.get(i).label, x + 4, btnY + 2, hovered ? 0xFFFF55 : 0xFFFFFF);
        }

        RenderSystem.enableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || options == null || button != 0) return false;

        int height = options.size() * buttonHeight;
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            int index = (int) (mouseY - y) / buttonHeight;
            if (index >= 0 && index < options.size()) {
                options.get(index).execute();
                close();
                return true;
            }
        }
        close();
        return false;
    }

    public static class ActionOption {
        String label;
        Runnable action;

        public ActionOption(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }
        public void execute() { action.run(); }
    }
}