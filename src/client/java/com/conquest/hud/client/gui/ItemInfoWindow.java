package com.conquest.hud.client.gui;

import com.conquest.hud.core.config.ItemWeightConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ItemInfoWindow extends ModularWindow {
    private ItemStack displayStack = ItemStack.EMPTY;

    public ItemInfoWindow(String title, int defaultX, int defaultY) {
        super(title, defaultX, defaultY, 160, 180, false);
    }

    public void showItem(ItemStack stack) {
        this.displayStack = stack.copy();
        this.setVisible(true);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        if (!isVisible() || displayStack.isEmpty()) return;

        MinecraftClient client = MinecraftClient.getInstance();

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 500); // Фикс перекрытия: поднимаем контент окна

        // Скрытый фон для маскирования предметов инвентаря под окном
        context.fill(x, y, x + width, y + height, 0x01000000);

        // Отрисовка предмета
        context.getMatrices().push();
        context.getMatrices().translate(x + (width / 2f) - 16, y + 35, 0);
        context.getMatrices().scale(2.0f, 2.0f, 1.0f);
        context.drawItem(displayStack, 0, 0);
        this.drawCustomItemOverlay(context, client.textRenderer, displayStack, false);
        context.getMatrices().pop();

        String name = displayStack.getName().getString();
        int nameWidth = client.textRenderer.getWidth(name);
        context.drawTextWithShadow(client.textRenderer, name, x + (width / 2) - (nameWidth / 2), y + 90, 0xFFAAAA);

        float weight = ItemWeightConfig.getWeight(displayStack) * displayStack.getCount();
        context.drawTextWithShadow(client.textRenderer, "Тип: " + (displayStack.isFood() ? "Провизия" : "Предмет"), x + 10, y + 115, 0xDDDDDD);
        context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "Вес: %.2f кг", weight), x + 10, y + 130, 0xDDDDDD);

        if (displayStack.getMaxCount() > 1) {
            context.drawTextWithShadow(client.textRenderer, "В стаке: " + displayStack.getCount() + " шт.", x + 10, y + 145, 0xDDDDDD);
        }

        context.getMatrices().pop();
    }
}