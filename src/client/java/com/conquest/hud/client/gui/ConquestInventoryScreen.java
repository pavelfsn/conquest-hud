package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.NanoVGHelper;
import com.conquest.hud.core.inventory.ConquestScreenHandler;
import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import com.conquest.hud.mixin.SlotAccessor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;


public class ConquestInventoryScreen extends HandledScreen<ConquestScreenHandler> {
    public static final int FLAG_INVENTORY = 1;
    public static final int FLAG_EQUIPMENT = 2;
    public static int activeFlags = 0;

    private static final int SLOT_SIZE = 32;
    private static final int SLOT_STEP = 36; // 32px слот + 4px зазор
    private static final int INVENTORY_WIDTH = 344; // Уменьшено под новую сетку
    private static final int INVENTORY_HEIGHT = 194; // Уменьшено под новую сетку
    private static final int EQUIPMENT_BASE_WIDTH = 340;
    private static final int EQUIPMENT_HEIGHT = 320;
    private static final int STATS_WIDTH = 170;

    private boolean statsWindowVisible = true;
    private float currentStatsWidth = 170f;

    private boolean draggingEquipment = false;
    private boolean draggingInventory = false;
    private int dragOffsetX, dragOffsetY;

    public ConquestInventoryScreen(ConquestScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 800;
        this.backgroundHeight = 600;
    }

    @Override public boolean shouldPause() { return false; }
    @Override public void close() { WindowPositionConfig.save(); super.close(); }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (com.conquest.hud.client.ConquestKeybinds.inventoryKey.matchesKey(keyCode, scanCode)) {
            activeFlags ^= FLAG_INVENTORY;
            if (activeFlags == 0) this.close();
            return true;
        }
        if (com.conquest.hud.client.ConquestKeybinds.equipmentKey.matchesKey(keyCode, scanCode)) {
            activeFlags ^= FLAG_EQUIPMENT;
            if (activeFlags == 0) this.close();
            return true;
        }
        var key = net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, scanCode);
        if (this.client != null && this.client.options != null && (
                this.client.options.forwardKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.backKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.leftKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.rightKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.jumpKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.sprintKey.matchesKey(keyCode, scanCode))) {
            net.minecraft.client.option.KeyBinding.setKeyPressed(key, true);
            net.minecraft.client.option.KeyBinding.onKeyPressed(key);
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        var key = net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, scanCode);
        if (this.client != null && this.client.options != null && (
                this.client.options.forwardKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.backKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.leftKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.rightKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.jumpKey.matchesKey(keyCode, scanCode) ||
                        this.client.options.sprintKey.matchesKey(keyCode, scanCode))) {
            net.minecraft.client.option.KeyBinding.setKeyPressed(key, false);
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void init() {
        super.init();
        this.x = 0; this.y = 0; this.titleX = -9999; this.playerInventoryTitleX = -9999;
    }

    private void updateSlotPositions() {
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");
        boolean showInv = (activeFlags & FLAG_INVENTORY) != 0;
        boolean showEq = (activeFlags & FLAG_EQUIPMENT) != 0;

        int eqOffsetX = eqPos[0] + (int) currentStatsWidth + 10;
        int eqOffsetY = eqPos[1];

        // Инвентарь
        for (int i = 0; i < 36; i++) {
            Slot slot = this.handler.slots.get(i);
            if (showInv) {
                if (i < 27) {
                    ((SlotAccessor) slot).setX(invPos[0] + 10 + (i % 9) * SLOT_STEP);
                    ((SlotAccessor) slot).setY(invPos[1] + 30 + (i / 9) * SLOT_STEP);
                } else {
                    ((SlotAccessor) slot).setX(invPos[0] + 10 + (i - 27) * SLOT_STEP);
                    ((SlotAccessor) slot).setY(invPos[1] + 30 + 3 * SLOT_STEP + 10);
                }
            } else {
                ((SlotAccessor) slot).setX(-9999);
                ((SlotAccessor) slot).setY(-9999);
            }
        }

        // Экипировка (Слоты 36+ включают ванильную броню и Trinkets)
        if (showEq) {
            for (int i = 36; i < this.handler.slots.size(); i++) {
                Slot slot = this.handler.slots.get(i);
                int x = -9999, y = -9999;

                if (slot.inventory instanceof dev.emi.trinkets.api.TrinketInventory trinketInv) {
                    String group = trinketInv.getSlotType().getGroup();
                    String type = trinketInv.getSlotType().getName();

                    if (group.equals("head") && type.equals("helmet")) { x = eqOffsetX + 136; y = eqOffsetY + 40; }
                    else if (group.equals("head") && type.equals("mask")) { x = eqOffsetX + 172; y = eqOffsetY + 40; }
                    else if (group.equals("chest") && type.equals("backpack")) { x = eqOffsetX + 86; y = eqOffsetY + 100; }
                    else if (group.equals("chest") && type.equals("rig")) { x = eqOffsetX + 122; y = eqOffsetY + 100; }
                    else if (group.equals("chest") && type.equals("armor")) { x = eqOffsetX + 86; y = eqOffsetY + 136; }
                    else if (group.equals("weapon") && type.equals("primary")) { x = eqOffsetX + 122; y = eqOffsetY + 136; }
                    else if (group.equals("weapon") && type.equals("melee")) { x = eqOffsetX + 86; y = eqOffsetY + 172; }
                    else if (group.equals("weapon") && type.equals("secondary")) { x = eqOffsetX + 122; y = eqOffsetY + 172; }
                    else if (group.equals("chest") && type.equals("suit")) { x = eqOffsetX + 222; y = eqOffsetY + 100; }
                    else if (group.equals("chest") && type.equals("jacket")) { x = eqOffsetX + 186; y = eqOffsetY + 136; }
                    else if (group.equals("legs") && type.equals("pants")) { x = eqOffsetX + 186; y = eqOffsetY + 172; }
                    else if (group.equals("accessory")) {
                        int accIndex = Integer.parseInt(type.replace("slot", "")) - 1;
                        x = eqOffsetX + 28 + (accIndex * 36);
                        y = eqOffsetY + 240;
                    }
                }
                ((SlotAccessor) slot).setX(x);
                ((SlotAccessor) slot).setY(y);
            }
        } else {
            // ФИКС: Убираем слоты экипировки за экран, когда окно закрыто
            for (int i = 36; i < this.handler.slots.size(); i++) {
                Slot slot = this.handler.slots.get(i);
                ((SlotAccessor) slot).setX(-9999);
                ((SlotAccessor) slot).setY(-9999);
            }
        }
    }

    private Slot getHoveredSlot(double mouseX, double mouseY) {
        for (Slot slot : this.handler.slots) {
            if (slot.x != -9999 && slot.y != -9999 && mouseX >= slot.x && mouseX <= slot.x + SLOT_SIZE && mouseY >= slot.y && mouseY <= slot.y + SLOT_SIZE) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected boolean isClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button) {
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");
        int currentEqWidth = EQUIPMENT_BASE_WIDTH + (int) currentStatsWidth;

        boolean inInv = (activeFlags & FLAG_INVENTORY) != 0 && mouseX >= invPos[0] && mouseX <= invPos[0] + INVENTORY_WIDTH && mouseY >= invPos[1] && mouseY <= invPos[1] + INVENTORY_HEIGHT;
        boolean inEq = (activeFlags & FLAG_EQUIPMENT) != 0 && mouseX >= eqPos[0] && mouseX <= eqPos[0] + currentEqWidth && mouseY >= eqPos[1] && mouseY <= eqPos[1] + EQUIPMENT_HEIGHT;

        return !inInv && !inEq;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);

        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");
        int currentEqWidth = EQUIPMENT_BASE_WIDTH + (int) currentStatsWidth;

        if ((activeFlags & FLAG_EQUIPMENT) != 0 && mouseX >= eqPos[0] + 8 && mouseX <= eqPos[0] + 24 && mouseY >= eqPos[1] + 4 && mouseY <= eqPos[1] + 20) {
            statsWindowVisible = !statsWindowVisible;
            return true;
        }

        Slot clickedSlot = this.getHoveredSlot(mouseX, mouseY);
        if (clickedSlot != null) {
            var action = net.minecraft.client.gui.screen.Screen.hasShiftDown() ? net.minecraft.screen.slot.SlotActionType.QUICK_MOVE : net.minecraft.screen.slot.SlotActionType.PICKUP;

            // Проверка: экипировка или Shift-клик отправляются на сервер для задержки
            if (clickedSlot.id >= 36 || action == net.minecraft.screen.slot.SlotActionType.QUICK_MOVE) {
                net.minecraft.network.PacketByteBuf buf = net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();
                buf.writeInt(clickedSlot.id);
                buf.writeInt(button);
                buf.writeInt(action.ordinal());

                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        com.conquest.hud.ConquestPackets.EQUIP_CLICK_PACKET,
                        buf
                );
                return true;
            }

            // Обычное перетаскивание внутри инвентаря выполняется мгновенно
            this.onMouseClick(clickedSlot, clickedSlot.id, button, action);
            return true;
        }

        if ((activeFlags & FLAG_INVENTORY) != 0 && mouseX >= invPos[0] + INVENTORY_WIDTH - 22 && mouseX <= invPos[0] + INVENTORY_WIDTH - 4 && mouseY >= invPos[1] + 4 && mouseY <= invPos[1] + 20) {
            activeFlags &= ~FLAG_INVENTORY;
            if (activeFlags == 0) this.close();
            return true;
        }

        if ((activeFlags & FLAG_EQUIPMENT) != 0 && mouseX >= eqPos[0] + currentEqWidth - 22 && mouseX <= eqPos[0] + currentEqWidth - 4 && mouseY >= eqPos[1] + 4 && mouseY <= eqPos[1] + 20) {
            activeFlags &= ~FLAG_EQUIPMENT;
            if (activeFlags == 0) this.close();
            return true;
        }

        if ((activeFlags & FLAG_INVENTORY) != 0 && mouseX >= invPos[0] && mouseX <= invPos[0] + INVENTORY_WIDTH && mouseY >= invPos[1] && mouseY <= invPos[1] + 24) {
            draggingInventory = true; dragOffsetX = (int)mouseX - invPos[0]; dragOffsetY = (int)mouseY - invPos[1]; return true;
        }

        if ((activeFlags & FLAG_EQUIPMENT) != 0 && mouseX >= eqPos[0] && mouseX <= eqPos[0] + currentEqWidth && mouseY >= eqPos[1] && mouseY <= eqPos[1] + 24) {
            draggingEquipment = true; dragOffsetX = (int)mouseX - eqPos[0]; dragOffsetY = (int)mouseY - eqPos[1]; return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { draggingInventory = false; draggingEquipment = false; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean isIntersecting(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        int screenW = this.client != null ? this.client.getWindow().getScaledWidth() : 0;
        int screenH = this.client != null ? this.client.getWindow().getScaledHeight() : 0;
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");
        int curEqW = EQUIPMENT_BASE_WIDTH + (int) currentStatsWidth;

        if (draggingInventory) {
            int newX = Math.max(0, Math.min((int)mouseX - dragOffsetX, screenW - INVENTORY_WIDTH));
            int newY = Math.max(0, Math.min((int)mouseY - dragOffsetY, screenH - INVENTORY_HEIGHT));
            if ((activeFlags & FLAG_EQUIPMENT) != 0 && isIntersecting(newX, newY, INVENTORY_WIDTH, INVENTORY_HEIGHT, eqPos[0], eqPos[1], curEqW, EQUIPMENT_HEIGHT)) {
                return true;
            }
            WindowPositionConfig.set("inventory", newX, newY);
            return true;
        }
        if (draggingEquipment) {
            int newX = Math.max(0, Math.min((int)mouseX - dragOffsetX, screenW - curEqW));
            int newY = Math.max(0, Math.min((int)mouseY - dragOffsetY, screenH - EQUIPMENT_HEIGHT));
            if ((activeFlags & FLAG_INVENTORY) != 0 && isIntersecting(newX, newY, curEqW, EQUIPMENT_HEIGHT, invPos[0], invPos[1], INVENTORY_WIDTH, INVENTORY_HEIGHT)) {
                return true;
            }
            WindowPositionConfig.set("equipment", newX, newY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override public void renderBackground(DrawContext context) {}

    private void drawExactBorder(NanoVGHelper nvg, float x, float y, float w, float h) {
        // Идеальная математика обводки без пересечения углов
        nvg.drawRoundedRect(x, y, w, 1, 0.0f, 0x333333, 1.0f); // Верхняя линия
        nvg.drawRoundedRect(x, y, 1, h, 0.0f, 0x333333, 1.0f); // Левая линия
        nvg.drawRoundedRect(x + w - 1, y, 1, h, 0.0f, 0x333333, 1.0f); // Правая линия
        nvg.drawRoundedRect(x, y + h - 1, w, 1, 0.0f, 0x333333, 1.0f); // Нижняя линия
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        NanoVGHelper nvg = NanoVGHelper.INSTANCE;
        if (!nvg.isInitialized()) return;

        float targetWidth = statsWindowVisible ? STATS_WIDTH : 0f;
        if (Math.abs(currentStatsWidth - targetWidth) > 0.5f) {
            currentStatsWidth += (targetWidth - currentStatsWidth) * 0.2f;
        } else {
            currentStatsWidth = targetWidth;
        }
        updateSlotPositions();

        int screenWidth = this.client.getWindow().getScaledWidth();
        int screenHeight = this.client.getWindow().getScaledHeight();
        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");

        nvg.beginFrame(screenWidth, screenHeight);

        if ((activeFlags & FLAG_INVENTORY) != 0) {
            nvg.drawRoundedRect(invPos[0], invPos[1], INVENTORY_WIDTH, INVENTORY_HEIGHT, 0.0f, 0x0D0D0D, 0.95f);
            nvg.drawRoundedRect(invPos[0], invPos[1], INVENTORY_WIDTH, 24, 0.0f, 0x1F1F1F, 1.0f);
        }

        if ((activeFlags & FLAG_EQUIPMENT) != 0) {
            int currentEqWidth = EQUIPMENT_BASE_WIDTH + (int) currentStatsWidth;
            int eqOffsetX = eqPos[0] + (int) currentStatsWidth + 10;

            nvg.drawRoundedRect(eqPos[0], eqPos[1], currentEqWidth, EQUIPMENT_HEIGHT, 0.0f, 0x0D0D0D, 0.95f);
            nvg.drawRoundedRect(eqPos[0], eqPos[1], currentEqWidth, 24, 0.0f, 0x1F1F1F, 1.0f);

            if (currentStatsWidth > 10) {
                nvg.drawRoundedRect(eqPos[0], eqPos[1] + 24, currentStatsWidth, EQUIPMENT_HEIGHT - 24, 0.0f, 0x151515, 1.0f);
            }

            // Ровные подложки для групп Экипировки
            nvg.drawRoundedRect(eqOffsetX + 136, eqPos[1] + 24, 68, 12, 0.0f, 0x222222, 1.0f); // Шлем
            nvg.drawRoundedRect(eqOffsetX + 86, eqPos[1] + 84, 68, 12, 0.0f, 0x222222, 1.0f); // Снаряжение
            nvg.drawRoundedRect(eqOffsetX + 186, eqPos[1] + 84, 68, 12, 0.0f, 0x222222, 1.0f); // Верхняя одежда
            nvg.drawRoundedRect(eqOffsetX + 28, eqPos[1] + 224, 284, 12, 0.0f, 0x222222, 1.0f); // Карманы

            // Отрисовка всех рамок (включая пустышки) строго по координатам
            int[][] bgSlots = {
                    {136, 40}, {172, 40}, // Head
                    {86, 100}, {122, 100}, {86, 136}, {122, 136}, {86, 172}, {122, 172}, // Gear
                    {186, 100}, {222, 100}, {186, 136}, {222, 136}, {186, 172}, {222, 172}, // Outerwear
                    {28, 240}, {64, 240}, {100, 240}, {136, 240}, {172, 240}, {208, 240}, {244, 240}, {280, 240} // Pockets
            };
            for (int[] pos : bgSlots) {
                int x = eqOffsetX + pos[0];
                int y = eqPos[1] + pos[1];
                nvg.drawRoundedRect(x, y, SLOT_SIZE, SLOT_SIZE, 0.0f, 0x1A1A1A, 0.8f);
                drawExactBorder(nvg, x, y, SLOT_SIZE, SLOT_SIZE);
            }
        }

        for (Slot slot : this.handler.slots) {
            if (slot.x != -9999 && slot.y != -9999 && (activeFlags & FLAG_INVENTORY) != 0 && slot.id < 36) {
                nvg.drawRoundedRect(slot.x, slot.y, SLOT_SIZE, SLOT_SIZE, 0.0f, 0x1A1A1A, 0.8f);
                drawExactBorder(nvg, slot.x, slot.y, SLOT_SIZE, SLOT_SIZE);
            }
        }
        nvg.endFrame();
        org.lwjgl.opengl.GL30.glBindVertexArray(0);
    }

    private void drawStatLine(DrawContext ctx, String name, float val, float max, float buff, int x, int y, int width) {
        String baseStr = String.format("%s: %.1f / %.1f", name, val, max);
        ctx.drawText(this.textRenderer, baseStr, x, y, 0xAAAAAA, false);
        if (buff > 0) {
            String buffStr = String.format("+%.1f", buff);
            int buffWidth = this.textRenderer.getWidth(buffStr);
            ctx.drawText(this.textRenderer, buffStr, x + width - buffWidth - 16, y, 0x55FF55, false);
        }
    }

    private boolean isSlotEmptyAt(int x, int y) {
        for (Slot slot : this.handler.slots) {
            if (slot.x == x && slot.y == y) {
                return !slot.hasStack();
            }
        }
        return true;
    }

    private void drawLabelIfEmpty(DrawContext ctx, String text, int x, int y) {
        if (isSlotEmptyAt(x, y)) {
            int w = this.textRenderer.getWidth(text);
            // 16 - центр слота 32x32. 12 - вертикальный центр
            ctx.drawText(this.textRenderer, text, x + 16 - w/2, y + 12, 0x888888, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Сначала рендерим фон и стандартные элементы HandledScreen
        super.render(context, mouseX, mouseY, delta);

        int[] invPos = WindowPositionConfig.get("inventory");
        int[] eqPos = WindowPositionConfig.get("equipment");

        if ((activeFlags & FLAG_INVENTORY) != 0) {
            context.drawText(this.textRenderer, "Инвентарь [I]", invPos[0] + 8, invPos[1] + 8, 0xFFFFFF, false);
            context.drawText(this.textRenderer, "X", invPos[0] + INVENTORY_WIDTH - 18, invPos[1] + 7, 0xAAAAAA, false);
        }

        if ((activeFlags & FLAG_EQUIPMENT) != 0) {
            int currentEqWidth = EQUIPMENT_BASE_WIDTH + (int) currentStatsWidth;
            int eqOffsetX = eqPos[0] + (int) currentStatsWidth + 10;

            context.drawText(this.textRenderer, statsWindowVisible ? "◀" : "▶", eqPos[0] + 8, eqPos[1] + 8, 0xAAAAAA, false);
            context.drawText(this.textRenderer, "ЭКИПИРОВКА", eqPos[0] + (currentEqWidth / 2) - 30, eqPos[1] + 8, 0xFFFFFF, false);
            context.drawText(this.textRenderer, "X", eqPos[0] + currentEqWidth - 18, eqPos[1] + 7, 0xAAAAAA, false);

            // Текст на серых плашках над группами
            context.drawText(this.textRenderer, "Шлем и маска", eqOffsetX + 136 + (68 - this.textRenderer.getWidth("Шлем и маска")) / 2, eqPos[1] + 26, 0xDDDDDD, false);
            context.drawText(this.textRenderer, "Снаряжение", eqOffsetX + 86 + (68 - this.textRenderer.getWidth("Снаряжение")) / 2, eqPos[1] + 86, 0xDDDDDD, false);
            context.drawText(this.textRenderer, "Верхняя одежда", eqOffsetX + 186 + (68 - this.textRenderer.getWidth("Верхняя одежда")) / 2, eqPos[1] + 86, 0xDDDDDD, false);
            context.drawText(this.textRenderer, "Карманы", eqOffsetX + 28 + 4, eqPos[1] + 226, 0xDDDDDD, false);

            // Водяные знаки в пустых слотах
            drawLabelIfEmpty(context, "Шлем", eqOffsetX + 136, eqPos[1] + 40);
            drawLabelIfEmpty(context, "Маска", eqOffsetX + 172, eqPos[1] + 40);

            drawLabelIfEmpty(context, "Рюкзак", eqOffsetX + 86, eqPos[1] + 100);
            drawLabelIfEmpty(context, "Разгрузка", eqOffsetX + 122, eqPos[1] + 100);
            drawLabelIfEmpty(context, "Броня", eqOffsetX + 86, eqPos[1] + 136);
            drawLabelIfEmpty(context, "Оружие", eqOffsetX + 122, eqPos[1] + 136);
            drawLabelIfEmpty(context, "Нож", eqOffsetX + 86, eqPos[1] + 172);
            drawLabelIfEmpty(context, "Пистолет", eqOffsetX + 122, eqPos[1] + 172);

            drawLabelIfEmpty(context, "Костюм", eqOffsetX + 222, eqPos[1] + 100); // Правый слот
            drawLabelIfEmpty(context, "Куртка", eqOffsetX + 186, eqPos[1] + 136);
            drawLabelIfEmpty(context, "Костюм", eqOffsetX + 222, eqPos[1] + 136);
            drawLabelIfEmpty(context, "Штаны", eqOffsetX + 186, eqPos[1] + 172);
            drawLabelIfEmpty(context, "Костюм", eqOffsetX + 222, eqPos[1] + 172);

            for (int i = 1; i <= 8; i++) {
                drawLabelIfEmpty(context, "Слот " + i, eqOffsetX + 28 + ((i-1) * 36), eqPos[1] + 240);
            }

            // Рендер панели характеристик
            if (currentStatsWidth > 150 && this.client != null && this.client.player != null) {
                IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(this.client.player);
                if (stats != null) {
                    int sx = eqPos[0] + 8;
                    int sy = eqPos[1] + 35;
                    context.drawText(this.textRenderer, "▼ ХАРАКТЕРИСТИКИ", sx, sy, 0xFFFFFF, false);

                    drawStatLine(context, "Здоровье", this.client.player.getHealth(), this.client.player.getMaxHealth(), 15.0f, sx, sy + 20, (int)currentStatsWidth);
                    drawStatLine(context, "Стамина", 100f, 100f, 5.0f, sx, sy + 32, (int)currentStatsWidth);
                    drawStatLine(context, "Реген HP", 1.0f, 1.0f, 0.2f, sx, sy + 44, (int)currentStatsWidth);
                    drawStatLine(context, "Реген Стамины", 0.3f, 0.3f, 0.0f, sx, sy + 56, (int)currentStatsWidth);

                    float curW = WeightManager.getCurrentWeight(this.client.player);
                    float maxW = WeightManager.getMaxWeight(this.client.player);
                    drawStatLine(context, "Вес", curW, maxW, 0.0f, sx, sy + 76, (int)currentStatsWidth);
                }
            }
        }
    }
}