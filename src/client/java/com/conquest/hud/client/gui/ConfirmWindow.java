package com.conquest.hud.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class ConfirmWindow extends ModularWindow {
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final String message;

    public ConfirmWindow(String message, int x, int y, Runnable onConfirm, Runnable onCancel) {
        super("Подтверждение", x, y, 220, 80, true);
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        if (!isVisible()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(message);
        context.drawTextWithShadow(client.textRenderer, message, x + (width / 2) - (textWidth / 2), y + 30, 0xFFFFFF);

        boolean hoverYes = isHoveredBtn(mouseX, mouseY, x + 30, y + 50, 60, 20);
        context.fill(x + 30, y + 50, x + 90, y + 70, hoverYes ? 0x6600FF00 : 0x4400FF00);
        context.drawBorder(x + 30, y + 50, 60, 20, 0xFF00FF00);
        context.drawTextWithShadow(client.textRenderer, "Да", x + 52, y + 56, 0xFFFFFF);

        boolean hoverNo = isHoveredBtn(mouseX, mouseY, x + 130, y + 50, 60, 20);
        context.fill(x + 130, y + 50, x + 190, y + 70, hoverNo ? 0x66FF0000 : 0x44FF0000);
        context.drawBorder(x + 130, y + 50, 60, 20, 0xFFFF0000);
        context.drawTextWithShadow(client.textRenderer, "Нет", x + 148, y + 56, 0xFFFFFF);
    }

    private boolean isHoveredBtn(int mx, int my, int bx, int by, int bw, int bh) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;
        if (button == 0) {
            if (isHoveredBtn((int)mouseX, (int)mouseY, x + 30, y + 50, 60, 20)) {
                onConfirm.run();
                this.isClosed = true;
                this.setVisible(false);
                return true;
            }
            if (isHoveredBtn((int)mouseX, (int)mouseY, x + 130, y + 50, 60, 20)) {
                if (onCancel != null) onCancel.run();
                this.isClosed = true;
                this.setVisible(false);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}