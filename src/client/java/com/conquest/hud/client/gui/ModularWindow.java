package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.nanovg.NanoVGManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class ModularWindow {
    public int x, y, width, height;
    protected String title;
    protected boolean isDragging = false;
    protected int dragOffsetX = 0;
    protected int dragOffsetY = 0;
    protected boolean visible;
    public boolean isClosed = false;

    public ModularWindow(String title, int defaultX, int defaultY, int width, int height, boolean defaultVisible) {
        this.title = title;
        this.width = width;
        this.height = height;

        int[] pos = WindowPositionConfig.get(title);
        if (pos == null || (pos[0] <= 0 && pos[1] <= 0) || (pos[0] == 100 && pos[1] == 100)) {
            WindowPositionConfig.set(title, defaultX, defaultY);
            this.x = defaultX;
            this.y = defaultY;
        } else {
            this.x = pos[0];
            this.y = pos[1];
        }
        this.visible = defaultVisible;
    }

    public void toggle() { this.visible = !this.visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public boolean isVisible() { return this.visible; }

    public void renderNanoVGBackground(int mouseX, int mouseY) {
        if (!visible) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();

        if (isDragging) {
            this.x = mouseX - dragOffsetX;
            this.y = mouseY - dragOffsetY;
        }

        // ЖЕСТКИЙ ФИКС ВЫХОДА ЗА ЭКРАН
        this.x = Math.max(0, Math.min(this.x, sw - this.width));
        this.y = Math.max(0, Math.min(this.y, sh - 20)); // Разрешаем скрыть низ, но шапка всегда на экране

        int bgColor = (title.contains("[U]") || title.equals("Инвентарь")) ? 0xFF0B0A09 : 0xF20B0A09;
        int headerColor = (title.contains("[U]") || title.equals("Инвентарь")) ? 0xFF242424 : 0xF2242424;

        NanoVGManager.drawRoundedRect(x, y, width, height, 0.0f, bgColor);
        NanoVGManager.drawRoundedRect(x, y, width, 18, 0.0f, headerColor);
        NanoVGManager.drawRoundedRectStroke(x, y, width, height, 0.0f, 1.0f, 0xFF464646);
    }

    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        context.getMatrices().push();
        float scale = title.equals("Инвентарь") ? 1.1f : 1.0f;
        context.getMatrices().scale(scale, scale, 1.0f);

        if (title.contains("[U]")) {
            int textW = MinecraftClient.getInstance().textRenderer.getWidth(title);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, title, (int)((x + (width/2) - (textW/2)) / scale), (int)((y + 5) / scale), 0xFFFFFF);
        } else {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, title, (int)((x + 8) / scale), (int)((y + 5) / scale), 0xFFFFFF);
        }
        context.getMatrices().pop();

        int closeX = x + width - 16;
        int closeY = y + 1;
        boolean hovered = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 300);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "X", closeX + 4, closeY + 4, hovered ? 0xFF5555 : 0xAAAAAA);
        context.getMatrices().pop();
    }

    public void drawCustomItemOverlay(DrawContext context, TextRenderer textRenderer, ItemStack stack, boolean showWeight) {
        if (stack.isEmpty()) return;
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);

        if (stack.getCount() > 1) {
            context.getMatrices().push();
            context.getMatrices().translate(16, 0, 0);
            context.getMatrices().scale(0.6f, 0.6f, 1.0f);
            String count = String.valueOf(stack.getCount());
            int textWidth = textRenderer.getWidth(count);
            context.drawTextWithShadow(textRenderer, count, -textWidth, 2, 0xFFFFFF);
            context.getMatrices().pop();
        }
        context.getMatrices().pop();
    }

    // УНИВЕРСАЛЬНЫЙ ТУЛТИП В СТИЛЕ МОДА
    public static void drawConquestTooltip(DrawContext context, TextRenderer tr, List<Text> lines, int mouseX, int mouseY, int sw, int sh) {
        if (lines == null || lines.isEmpty()) return;

        int maxWidth = 0;
        for (Text line : lines) {
            int w = tr.getWidth(line);
            if (w > maxWidth) maxWidth = w;
        }

        int tW = maxWidth + 12;
        int tH = lines.size() * 10 + 6;
        int tX = mouseX + 12;
        int tY = mouseY - 12;

        if (tX + tW > sw) tX = mouseX - 12 - tW;
        if (tY + tH > sh) tY = sh - tH - 2;

        RenderSystem.disableDepthTest();
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 2000);

        context.fill(tX - 1, tY - 1, tX + tW + 1, tY + tH + 1, 0xFF000000);
        context.fill(tX, tY, tX + tW, tY + tH, 0xFF555555);
        context.fill(tX + 1, tY + 1, tX + tW - 1, tY + tH - 1, 0xEE1D1D1D);

        for (int i = 0; i < lines.size(); i++) {
            context.drawTextWithShadow(tr, lines.get(i), tX + 6, tY + 4 + (i * 10), 0xFFFFFF);
        }

        context.getMatrices().pop();
        RenderSystem.enableDepthTest();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        int closeX = x + width - 16;
        int closeY = y + 1;
        if (button == 0 && mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
            this.isClosed = true;
            this.setVisible(false);
            return true;
        }

        if (button == 0 && mouseX >= x && mouseX <= x + width - 20 && mouseY >= y && mouseY <= y + 18) {
            isDragging = true;
            dragOffsetX = (int) mouseX - x;
            dragOffsetY = (int) mouseY - y;
            return true;
        }

        // ВНИМАНИЕ: Возвращаем true ТОЛЬКО если это базовое окно. В дочерних переопределяется.
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isDragging && button == 0) {
            isDragging = false;
            WindowPositionConfig.set(title, x, y);
            return true;
        }
        return false;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return visible && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public String getTitle() { return title; }
}