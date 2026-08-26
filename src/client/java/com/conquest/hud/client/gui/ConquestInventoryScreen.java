package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.NanoVGHelper;
import com.conquest.hud.core.inventory.ConquestScreenHandler;
import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import com.conquest.hud.mixin.SlotAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;

public class ConquestInventoryScreen extends HandledScreen<ConquestScreenHandler> {
    public static int activeTab = 0; // 0 - Инвентарь, 1 - Экипировка, 2 - Оба

    // Состояние перетаскивания окон
    private boolean draggingEquipment = false;
    private boolean draggingInventory = false;
    private int dragOffsetX, dragOffsetY;

    public ConquestInventoryScreen(ConquestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 800;
        this.backgroundHeight = 600;
    }

    @Override
    protected void init() {
        super.init();
        this.x = 0;
        this.y = 0;
        this.titleX = -9999;
        this.playerInventoryTitleX = -9999;
        updateSlotPositions();
    }

    public void updateSlotPositions() {
        int cellSize = 36;
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");

        // Слоты 0-35 (Инвентарь и Хотбар)
        for (int i = 0; i < 36; i++) {
            Slot slot = this.handler.slots.get(i);
            if (activeTab == 0 || activeTab == 2) {
                if (i < 27) {
                    int row = i / 9;
                    int col = i % 9;
                    ((SlotAccessor) slot).setX(invPos[0] + 10 + col * cellSize + 2);
                    ((SlotAccessor) slot).setY(invPos[0] >= 0 ? invPos[1] + 30 + row * cellSize + 2 : 112);
                } else {
                    int col = i - 27;
                    ((SlotAccessor) slot).setX(invPos[0] + 10 + col * cellSize + 2);
                    ((SlotAccessor) slot).setY(invPos[1] + 30 + 3 * cellSize + 10 + 2);
                }
            } else {
                ((SlotAccessor) slot).setX(-9999);
                ((SlotAccessor) slot).setY(-9999);
            }
        }

        // Слоты 36-39 (Экипировка)
        for (int i = 36; i < 40; i++) {
            Slot slot = this.handler.slots.get(i);
            if (activeTab == 1 || activeTab == 2) {
                ((SlotAccessor) slot).setX(eqPos[0] + 10 + 2);
                ((SlotAccessor) slot).setY(eqPos[1] + 30 + (i - 36) * cellSize + 2);
            } else {
                ((SlotAccessor) slot).setX(-9999);
                ((SlotAccessor) slot).setY(-9999);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int[] invPos = WindowPositionConfig.get("inventory");
            int[] eqPos = WindowPositionConfig.get("equipment");

            // Проверка клика по кнопке закрытия Инвентаря [X]
            if ((activeTab == 0 || activeTab == 2) && mouseX >= invPos[0] + 322 && mouseX <= invPos[0] + 340 && mouseY >= invPos[1] + 4 && mouseY <= invPos[1] + 20) {
                if (activeTab == 2) activeTab = 1; else { this.close(); return true; }
                updateSlotPositions();
                return true;
            }
            // Проверка клика по кнопке закрытия Экипировки [X]
            if ((activeTab == 1 || activeTab == 2) && mouseX >= eqPos[0] + 182 && mouseX <= eqPos[0] + 200 && mouseY >= eqPos[1] + 4 && mouseY <= eqPos[1] + 20) {
                if (activeTab == 2) activeTab = 0; else { this.close(); return true; }
                updateSlotPositions();
                return true;
            }

            // Захват шапки Инвентаря для перетаскивания
            if ((activeTab == 0 || activeTab == 2) && mouseX >= invPos[0] && mouseX <= invPos[0] + 344 && mouseY >= invPos[1] && mouseY <= invPos[1] + 24) {
                draggingInventory = true;
                dragOffsetX = (int)mouseX - invPos[0];
                dragOffsetY = (int)mouseY - invPos[1];
                return true;
            }
            // Захват шапки Экипировки для перетаскивания
            if ((activeTab == 1 || activeTab == 2) && mouseX >= eqPos[0] && mouseX <= eqPos[0] + 200 && mouseY >= eqPos[1] && mouseY <= eqPos[1] + 24) {
                draggingEquipment = true;
                dragOffsetX = (int)mouseX - eqPos[0];
                dragOffsetY = (int)mouseY - eqPos[1];
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (draggingInventory || draggingEquipment) {
                WindowPositionConfig.save();
            }
            draggingInventory = false;
            draggingEquipment = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingInventory) {
            int newX = (int)mouseX - dragOffsetX;
            int newY = (int)mouseY - dragOffsetY;
            WindowPositionConfig.set("inventory", newX, newY);
            updateSlotPositions();
            return true;
        }
        if (draggingEquipment) {
            int newX = (int)mouseX - dragOffsetX;
            int newY = (int)mouseY - dragOffsetY;
            WindowPositionConfig.set("equipment", newX, newY);
            updateSlotPositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        NanoVGHelper nvg = NanoVGHelper.INSTANCE;
        int screenWidth = this.client.getWindow().getScaledWidth();
        int screenHeight = this.client.getWindow().getScaledHeight();
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");

        nvg.beginFrame(screenWidth, screenHeight);

        // 1. Окно Инвентаря
        if (activeTab == 0 || activeTab == 2) {
            nvg.drawRoundedRect(invPos[0], invPos[1], 344, 210, 0.0f, 0x0D0D0D, 0.95f); // Фон
            nvg.drawRoundedRect(invPos[0], invPos[1], 344, 24, 0.0f, 0x1F1F1F, 1.0f);   // Шапка

            for (int i = 0; i < 36; i++) {
                Slot slot = this.handler.slots.get(i);
                nvg.drawRoundedRect(slot.x - 2, slot.y - 2, 36, 36, 0.0f, 0x1A1A1A, 0.8f);
                drawBorder(nvg, slot.x - 2, slot.y - 2, 36, 36);
            }
        }

        // 2. Окно Экипировки и Статов
        if (activeTab == 1 || activeTab == 2) {
            nvg.drawRoundedRect(eqPos[0], eqPos[1], 200, 250, 0.0f, 0x0D0D0D, 0.95f); // Фон
            nvg.drawRoundedRect(eqPos[0], eqPos[1], 200, 24, 0.0f, 0x1F1F1F, 1.0f);   // Шапка

            // Слоты брони
            for (int i = 36; i < 40; i++) {
                Slot slot = this.handler.slots.get(i);
                nvg.drawRoundedRect(slot.x - 2, slot.y - 2, 36, 36, 0.0f, 0x1A1A1A, 0.8f);
                drawBorder(nvg, slot.x - 2, slot.y - 2, 36, 36);
            }
        }

        nvg.endFrame();
    }

    private void drawBorder(NanoVGHelper nvg, float x, float y, float w, float h) {
        nvg.drawRoundedRect(x, y, w, 1, 0.0f, 0x333333, 1.0f);
        nvg.drawRoundedRect(x, y, 1, h, 0.0f, 0x333333, 1.0f);
        nvg.drawRoundedRect(x + w - 1, y, 1, h, 0.0f, 0x333333, 1.0f);
        nvg.drawRoundedRect(x, y + h - 1, w, 1, 0.0f, 0x333333, 1.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        net.minecraft.item.ItemStack cursorStack = this.handler.getCursorStack();
        this.handler.setCursorStack(net.minecraft.item.ItemStack.EMPTY);

        super.render(context, mouseX, mouseY, delta);

        this.handler.setCursorStack(cursorStack);

        if (!cursorStack.isEmpty()) {
            context.getMatrices().push();
            context.getMatrices().translate(mouseX, mouseY, 232.0f);
            context.getMatrices().scale(2.0f, 2.0f, 1.0f);
            context.drawItem(cursorStack, -8, -8);
            context.drawItemInSlot(this.textRenderer, cursorStack, -8, -8);
            context.getMatrices().pop();
        }

        this.drawMouseoverTooltip(context, mouseX, mouseY);

        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");

        if (activeTab == 0 || activeTab == 2) {
            context.drawText(this.textRenderer, "Инвентарь [I]", invPos[0] + 8, invPos[1] + 8, 0xFFFFFF, false);
            context.drawText(this.textRenderer, "X", invPos[0] + 328, invPos[1] + 7, 0xAAAAAA, false);
        }

        if (activeTab == 1 || activeTab == 2) {
            context.drawText(this.textRenderer, "Экипировка", eqPos[0] + 8, eqPos[1] + 8, 0xFFFFFF, false);
            context.drawText(this.textRenderer, "X", eqPos[0] + 188, eqPos[1] + 7, 0xAAAAAA, false);

            // Отрисовка блока статов прямо в окне экипировки
            PlayerEntity player = this.client.player;
            if (player != null) {
                IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
                int sx = eqPos[0] + 55;
                int sy = eqPos[1] + 32;

                context.drawText(this.textRenderer, "Сила: " + stats.getStrength(), sx, sy, 0xFFFFFF, false);
                context.drawText(this.textRenderer, "Ловкость: " + stats.getAgility(), sx, sy + 12, 0xFFFFFF, false);
                context.drawText(this.textRenderer, "Выносливость: " + stats.getVitality(), sx, sy + 24, 0xFFFFFF, false);
                context.drawText(this.textRenderer, "Метаболизм: " + stats.getMetabolism(), sx, sy + 36, 0xFFFFFF, false);
                context.drawText(this.textRenderer, "Интеллект: " + stats.getIntellect(), sx, sy + 48, 0xFFFFFF, false);

                float curW = WeightManager.getCurrentWeight(player);
                float maxW = WeightManager.getMaxWeight(player);
                context.drawText(this.textRenderer, String.format("Вес: %.1f/%.1f кг", curW, maxW), sx, sy + 68, 0xFFAA00, false);
            }
        }
    }
}