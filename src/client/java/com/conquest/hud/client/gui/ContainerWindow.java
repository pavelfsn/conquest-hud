package com.conquest.hud.client.gui;

import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.stats.WeightManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ContainerWindow extends ModularWindow {
    private final IContainer container;
    private final int columns;
    private final int slotSize = 32;
    private final int gap = 2; // Уплотнили слоты
    private final int step = slotSize + gap;

    public static boolean globalAutoSort = false;
    public boolean deleteMode = false;
    private int[] visualMapping;

    private ItemStack cursorStack = ItemStack.EMPTY;
    private int cursorSourceContainer = -1;
    private int cursorSourceSlot = -1;

    public ContainerWindow(String title, int defaultX, int defaultY, int width, int height, IContainer container, int columns, boolean defaultVisible) {
        super(title, defaultX, defaultY, width, height, defaultVisible);
        this.container = container;
        this.columns = columns;
    }

    public IContainer getContainer() {
        return this.container;
    }

    public void setCursorInfo(ItemStack cursor, int srcContainer, int srcSlot) {
        this.cursorStack = cursor;
        this.cursorSourceContainer = srcContainer;
        this.cursorSourceSlot = srcSlot;
    }

    public int getRealSlot(int visualIndex) {
        if (!globalAutoSort || visualMapping == null) return visualIndex;
        if (visualIndex >= 0 && visualIndex < visualMapping.length) return visualMapping[visualIndex];
        return visualIndex;
    }

    public int getHoveredVisualSlot(double mouseX, double mouseY) {
        if (!isVisible()) return -1;
        int startX = x + 5;
        int startY = y + 40;
        int maxSlots = container.getContainerId() == 0 ? 100 : container.getSize();

        for (int i = 0; i < maxSlots; i++) {
            int col = i % columns;
            int row = i / columns;
            int slotX = startX + col * step;
            int slotY = startY + row * step;

            if (mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize) {
                return i;
            }
        }
        return -1;
    }

    public int getHoveredSlot(double mouseX, double mouseY) {
        int visualSlot = getHoveredVisualSlot(mouseX, mouseY);
        return visualSlot != -1 ? getRealSlot(visualSlot) : -1;
    }

    public boolean isHoveredTrash(double mouseX, double mouseY) {
        if (!isVisible() || container.getContainerId() != 0) return false;
        int startY = y + 40;
        int maxSlots = container.getContainerId() == 0 ? 100 : container.getSize();
        int footerY = startY + (maxSlots / columns) * step - 2;
        int trashY = footerY + 8;
        int trashX = x + width - 35;
        return mouseX >= trashX && mouseX <= trashX + 20 && mouseY >= trashY && mouseY <= trashY + 20;
    }

    private void updateVisualMapping() {
        int size = container.getContainerId() == 0 ? 100 : container.getSize();
        visualMapping = new int[size];

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < size; i++) indices.add(i);

        indices.sort(Comparator.comparingInt(i -> {
            ItemStack stack = container.getStack(i);
            if (stack.isEmpty()) return 999;
            if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof net.minecraft.item.BowItem) return 1;
            if (stack.getItem() instanceof BlockItem) return 2;
            if (stack.isFood()) return 3;
            return 4;
        }));

        for (int i = 0; i < size; i++) {
            visualMapping[i] = indices.get(i);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        if (container.getContainerId() == 0) {
            int cbX = x + 10;
            int cbY = y + 23;
            if (button == 0 && mouseX >= cbX && mouseX <= cbX + 90 && mouseY >= cbY && mouseY <= cbY + 10) {
                globalAutoSort = !globalAutoSort;
                return true;
            }
            if (button == 0 && isHoveredTrash(mouseX, mouseY)) {
                deleteMode = !deleteMode;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        if (!isVisible()) return;

        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (globalAutoSort) updateVisualMapping();

        if (container.getContainerId() == 0) {
            int cbX = x + 10;
            int cbY = y + 23;
            context.fill(cbX, cbY, cbX + 10, cbY + 10, 0xFF222222);
            context.drawBorder(cbX, cbY, 10, 10, 0xFF555555);
            if (globalAutoSort) context.drawTextWithShadow(tr, "X", cbX + 2, cbY + 1, 0xFF55FF55);

            context.getMatrices().push();
            context.getMatrices().scale(0.85f, 0.85f, 1.0f);
            context.drawTextWithShadow(tr, "Автосортировка", (int)((cbX + 15) / 0.85f), (int)((cbY + 2) / 0.85f), 0xFFAAAAAA);
            context.getMatrices().pop();

            // --- ЗАГЛУШКИ КАТЕГОРИЙ ---
            int catX = x + width - 117;
            int catY = y + 22;
            net.minecraft.item.Item[] catItems = {
                    net.minecraft.item.Items.WOODEN_SWORD,
                    net.minecraft.item.Items.LEATHER_CHESTPLATE,
                    net.minecraft.item.Items.GOLD_NUGGET,
                    net.minecraft.item.Items.POTION,
                    net.minecraft.item.Items.ROTTEN_FLESH,
                    net.minecraft.item.Items.AMETHYST_SHARD,
                    net.minecraft.item.Items.BOOK
            };

            for (int j = 0; j < catItems.length; j++) {
                int px = catX + j * 16;
                context.fill(px, catY, px + 14, catY + 14, 0x88000000);
                context.drawBorder(px, catY, 14, 14, 0xFF555555);

                context.getMatrices().push();
                context.getMatrices().translate(px + 3, catY + 3, 0);
                context.getMatrices().scale(0.5f, 0.5f, 1.0f);
                context.drawItem(new ItemStack(catItems[j]), 0, 0);
                context.getMatrices().pop();
            }
        }

        int startX = x + 5;
        int startY = y + 40;
        int maxSlots = container.getContainerId() == 0 ? 100 : container.getSize();

        for (int i = 0; i < maxSlots; i++) {
            int col = i % columns;
            int row = i / columns;
            int slotX = startX + col * step;
            int slotY = startY + row * step;

            context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x88000000);

            boolean hovered = mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize;
            int realIndex = getRealSlot(i);
            ItemStack stack = container.getStack(realIndex);

            boolean isCursorSource = (container.getContainerId() == cursorSourceContainer
                    && realIndex == cursorSourceSlot
                    && !cursorStack.isEmpty());

            ItemStack stackToDraw = isCursorSource ? cursorStack : stack;

            if (deleteMode && hovered && !stack.isEmpty()) {
                context.drawBorder(slotX, slotY, slotSize, slotSize, 0xFFFFAA00);
                context.getMatrices().push();
                context.getMatrices().translate(0, 0, 200);
                context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x26FFAA00);
                context.getMatrices().pop();
            } else {
                context.drawBorder(slotX, slotY, slotSize, slotSize, 0xFF555555);
                if (hovered) {
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);
                    context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x44FFFFFF);
                    context.getMatrices().pop();
                }
            }

            if (!stackToDraw.isEmpty()) {
                context.getMatrices().push();
                context.getMatrices().translate(slotX + 4, slotY + 4, 250);
                context.getMatrices().scale(1.5f, 1.5f, 1.0f);

                if (isCursorSource) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
                }

                context.drawItem(stackToDraw, 0, 0);
                this.drawCustomItemOverlay(context, tr, stackToDraw, false);

                if (isCursorSource) {
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

                context.getMatrices().pop();
            }
        }

        if (container.getContainerId() == 0) {
            int occupied = 0;
            for (int i = 0; i < maxSlots; i++) {
                if (!container.getStack(i).isEmpty()) occupied++;
            }

            int footerY = startY + (maxSlots / columns) * step + 5;

            context.drawTextWithShadow(tr, "Слоты: " + occupied + " / " + maxSlots, x + 10, footerY + 5, 0xDDDDDD);
            float maxW = WeightManager.getMaxWeight(MinecraftClient.getInstance().player);
            float curW = WeightManager.getCurrentWeight(MinecraftClient.getInstance().player);
            context.drawTextWithShadow(tr, String.format(java.util.Locale.US, "Вес: %.1f / %.1f кг", curW, maxW), x + 10, footerY + 17, 0xDDDDDD);

            int trashX = x + width - 35;
            int currX = trashX - 140;
            String curRub = String.format(java.util.Locale.US, "%,d", 2147483647).replace(',', ' ');
            context.drawTextWithShadow(tr, curRub + " руб.", currX, footerY + 5, 0x55FF55);
            context.drawTextWithShadow(tr, "0 з.р.", currX, footerY + 17, 0xFFD700);

            int trashY = footerY + 8;
            boolean trashHovered = isHoveredTrash(mouseX, mouseY);

            context.fill(trashX, trashY, trashX + 20, trashY + 20, (trashHovered || deleteMode) ? 0x88FF0000 : 0x44FF0000);
            context.drawBorder(trashX, trashY, 20, 20, 0xFFFF0000);

            context.fill(trashX + 6, trashY + 9, trashX + 14, trashY + 11, 0xFFFFFFFF);
            context.fill(trashX + 9, trashY + 6, trashX + 11, trashY + 14, 0xFFFFFFFF);

            if (deleteMode) {
                context.drawTextWithShadow(tr, "УДАЛИТЬ", trashX - 60, trashY - 2, 0xFFFF5555);
                context.getMatrices().push();
                context.getMatrices().scale(0.8f, 0.8f, 1.0f);
                context.drawTextWithShadow(tr, "(выбранное)", (int)((trashX - 60) / 0.8f), (int)((trashY + 8) / 0.8f), 0xFFAAAAAA);
                context.getMatrices().pop();
            }
        }
    }
}