package com.conquest.hud.client.gui;

import com.conquest.hud.client.render.nanovg.NanoVGManager;
import com.conquest.hud.core.container.EquipmentSlot;
import com.conquest.hud.core.container.EquipmentSlotRegistry;
import com.conquest.hud.core.container.IContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EquipmentWindow extends ModularWindow {
    private final IContainer container;
    private final Map<Integer, int[]> slotPositions = new HashMap<>();
    private int activeTab = 0;

    private ItemStack cursorStack = ItemStack.EMPTY;
    private int cursorSourceContainer = -1;
    private int cursorSourceSlot = -1;

    private ItemStack lastCursorCheck = ItemStack.EMPTY;
    private final Map<Integer, Boolean> canEquipCache = new HashMap<>();

    public EquipmentWindow(String title, int defaultX, int defaultY, IContainer container, boolean defaultVisible) {
        super(title, defaultX, defaultY, 360, 417, defaultVisible);
        this.container = container;

        slotPositions.put(EquipmentSlot.HEAD.getIndex(), new int[]{50, 63});
        slotPositions.put(EquipmentSlot.MASK.getIndex(), new int[]{50, 118});
        slotPositions.put(EquipmentSlot.ARMOR.getIndex(), new int[]{50, 260});
        slotPositions.put(EquipmentSlot.TORSO.getIndex(), new int[]{50, 204});

        slotPositions.put(EquipmentSlot.BACKPACK.getIndex(), new int[]{262, 63});
        slotPositions.put(EquipmentSlot.RIG.getIndex(), new int[]{262, 118});
        slotPositions.put(EquipmentSlot.PANTS.getIndex(), new int[]{262, 204});
        slotPositions.put(EquipmentSlot.BOOTS.getIndex(), new int[]{262, 260});

        slotPositions.put(EquipmentSlot.PRIMARY_WEAPON.getIndex(), new int[]{120, 282});
        slotPositions.put(EquipmentSlot.SECONDARY_WEAPON.getIndex(), new int[]{192, 282});

        // Аксессуары подняты до y=340
        slotPositions.put(EquipmentSlot.ACCESSORY_1.getIndex(), new int[]{16, 340});
        slotPositions.put(EquipmentSlot.ACCESSORY_2.getIndex(), new int[]{72, 340});
        slotPositions.put(EquipmentSlot.ACCESSORY_3.getIndex(), new int[]{128, 340});
        slotPositions.put(EquipmentSlot.ACCESSORY_4.getIndex(), new int[]{184, 340});
        slotPositions.put(EquipmentSlot.ACCESSORY_5.getIndex(), new int[]{240, 340});
        slotPositions.put(EquipmentSlot.ACCESSORY_6.getIndex(), new int[]{296, 340});
    }

    public IContainer getContainer() {
        return this.container;
    }

    public void setCursorInfo(ItemStack cursor, int srcContainer, int srcSlot) {
        this.cursorStack = cursor;
        this.cursorSourceContainer = srcContainer;
        this.cursorSourceSlot = srcSlot;

        if (!ItemStack.areEqual(lastCursorCheck, cursor)) {
            lastCursorCheck = cursor.copy();
            canEquipCache.clear();
        }
    }

    public int getHoveredSlot(double mouseX, double mouseY) {
        if (!isVisible() || activeTab != 0) return -1;
        for (Map.Entry<Integer, int[]> entry : slotPositions.entrySet()) {
            int slotX = x + entry.getValue()[0];
            int slotY = y + entry.getValue()[1];
            if (mouseX >= slotX && mouseX < slotX + 48 && mouseY >= slotY && mouseY < slotY + 48) {
                return entry.getKey();
            }
        }
        return -1;
    }

    @Override
    public void renderNanoVGBackground(int mouseX, int mouseY) {
        if (!isVisible()) return;
        super.renderNanoVGBackground(mouseX, mouseY);

        int tabWidth = width / 4;
        for (int i = 0; i < 4; i++) {
            int tabX = x + (i * tabWidth);
            int tabY = y + 18;
            if (activeTab == i) {
                NanoVGManager.drawRoundedRect(tabX, tabY, tabWidth, 24, 0.0f, 0xFF444444);
                NanoVGManager.drawRoundedRectStroke(tabX, tabY, tabWidth, 24, 0.0f, 1.0f, 0xFF888888);
            } else {
                NanoVGManager.drawRoundedRect(tabX, tabY, tabWidth, 24, 0.0f, 0xFF222222);
            }
        }

        if (activeTab == 0) {
            NanoVGManager.drawRoundedRect(x + 111, y + 62, 139, 211, 0.0f, 0xFF151515);
            NanoVGManager.drawRoundedRectStroke(x + 111, y + 62, 139, 211, 0.0f, 1.0f, 0xFF333333);
        }
    }

    @Override
    public void renderForeground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderForeground(context, mouseX, mouseY, delta);
        if (!isVisible()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int tabWidth = width / 4;
        String[] tabs = {"Экипировка", "Статистика", "Характеристики", "Репутация"};
        for (int i = 0; i < 4; i++) {
            int textW = client.textRenderer.getWidth(tabs[i]);
            int tabX = x + (i * tabWidth) + (tabWidth / 2) - (textW / 2);
            context.drawTextWithShadow(client.textRenderer, tabs[i], tabX, y + 26, (activeTab == i) ? 0xFFFFFF : 0x888888);
        }

        if (activeTab == 0) {
            int entityX = x + 180;
            int entityY = y + 245;
            InventoryScreen.drawEntity(context, entityX, entityY, 70, (float)(entityX - mouseX), (float)(entityY - 100 - mouseY), client.player);

            for (Map.Entry<Integer, int[]> entry : slotPositions.entrySet()) {
                int slotIndex = entry.getKey();
                int slotX = x + entry.getValue()[0];
                int slotY = y + entry.getValue()[1];

                context.fill(slotX - 2, slotY - 2, slotX + 50, slotY + 50, 0xFF000000);
                context.drawBorder(slotX - 2, slotY - 2, 52, 52, 0xFF555555);
                context.fill(slotX, slotY, slotX + 48, slotY + 48, 0xFF1D1D1D);

                ItemStack stack = container.getStack(slotIndex);
                boolean isCursorSource = (container.getContainerId() == cursorSourceContainer
                        && slotIndex == cursorSourceSlot
                        && !cursorStack.isEmpty());
                ItemStack stackToDraw = isCursorSource ? cursorStack : stack;

                boolean canEquip = false;
                if (!cursorStack.isEmpty()) {
                    canEquip = canEquipCache.computeIfAbsent(slotIndex, idx ->
                            EquipmentSlotRegistry.canEquip(cursorStack, idx)
                    );
                }

                int borderColor = 0xFF555555;
                if (canEquip) {
                    borderColor = 0xFF00FF00;
                } else if (!cursorStack.isEmpty() && !canEquip && slotIndex != cursorSourceSlot) {
                    borderColor = 0xFFFF0000;
                }

                context.drawBorder(slotX, slotY, 48, 48, borderColor);

                if (!stackToDraw.isEmpty()) {
                    context.getMatrices().push();
                    context.getMatrices().translate(slotX + 8, slotY + 8, 250); // Фикс Z-индекса
                    context.getMatrices().scale(2.0f, 2.0f, 1.0f);

                    if (isCursorSource) {
                        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.5f);
                    }

                    context.drawItem(stackToDraw, 0, 0);
                    this.drawCustomItemOverlay(context, client.textRenderer, stackToDraw, false);

                    if (isCursorSource) {
                        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }

                    context.getMatrices().pop();
                }

                if (mouseX >= slotX && mouseX < slotX + 48 && mouseY >= slotY && mouseY < slotY + 48) {
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 200);
                    context.fill(slotX, slotY, slotX + 48, slotY + 48, 0x44FFFFFF);
                    context.getMatrices().pop();
                }
            }
        }
        if (activeTab == 1) { // Вкладка "Статистика"
            com.conquest.hud.core.progression.IProgressionComponent prog = com.conquest.hud.core.stats.StatsComponentRegistry.PROGRESSION.getNullable(client.player);
            if (prog != null) {
                int statX = x + 20;
                int statY = y + 60;
                int step = 20;

                float maxHp = 200f + (prog.getStat(2) * 10f);
                float hpRegen = prog.getStat(2) * 0.4f;
                float maxStam = 100f + (prog.getStat(1) * 10f);
                float stamRegen = 15f + (prog.getStat(1) * 1.5f);
                float maxWght = 40f + (prog.getStat(0) * 3f);

                context.drawTextWithShadow(client.textRenderer, "Здоровье:", statX, statY, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "%.0f ед.", maxHp), statX + 180, statY, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Регенерация здоровья:", statX, statY + step, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "%.1f ед/сек", hpRegen), statX + 180, statY + step, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Выносливость:", statX, statY + step*2, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "%.0f ед.", maxStam), statX + 180, statY + step*2, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Регенерация выносливости:", statX, statY + step*3, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "%.1f ед/сек", stamRegen), statX + 180, statY + step*3, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Скорость бега:", statX, statY + step*4, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, "100%", statX + 180, statY + step*4, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Грузоподъемность:", statX, statY + step*5, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, String.format(java.util.Locale.US, "%.1f кг", maxWght), statX + 180, statY + step*5, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Шанс добычи ресурсов:", statX, statY + step*6, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, "x1.0 (в разработке)", statX + 180, statY + step*6, 0xFFFFFF);

                context.drawTextWithShadow(client.textRenderer, "Кол-во получаемых ресурсов:", statX, statY + step*7, 0xAAAAAA);
                context.drawTextWithShadow(client.textRenderer, "x1.0 (в разработке)", statX + 180, statY + step*7, 0xFFFFFF);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isVisible()) return false;

        if (mouseY >= y + 18 && mouseY <= y + 42) {
            int tabWidth = width / 4;
            int clickedTab = (int) (mouseX - x) / tabWidth;
            if (clickedTab >= 0 && clickedTab < 4) {
                activeTab = clickedTab;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}