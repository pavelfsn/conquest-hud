package com.conquest.hud.core.inventory;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import dev.emi.trinkets.SurvivalTrinketSlot;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.Map;

public class ConquestScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<ConquestScreenHandler> TYPE;

    public ConquestScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(TYPE, syncId);

        // Индексы 0-26: Основной Инвентарь (Сетка 3x9)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 0, 0));
            }
        }

        // Индексы 27-35: Хотбар
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 0, 0));
        }

        // Индексы 36-39: Экипировка (Шлем, Нагрудник, Штаны, Ботинки)
        final EquipmentSlot[] EQUIPMENT = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipSlot = EQUIPMENT[i];
            this.addSlot(new Slot(playerInventory, 39 - i, 0, 0) {
                @Override
                public int getMaxItemCount() {
                    return 1;
                }

                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlotType() == equipSlot;
                }
            });
        }
        // Инициализация слотов Trinkets (индексы 40+)
        TrinketsApi.getTrinketComponent(playerInventory.player).ifPresent(trinkets -> {
            Map<String, dev.emi.trinkets.api.SlotGroup> groups = TrinketsApi.getEntitySlots(playerInventory.player.getType());
            trinkets.getInventory().forEach((groupId, groupMap) -> {
                dev.emi.trinkets.api.SlotGroup slotGroup = groups.get(groupId);
                if (slotGroup != null) {
                    groupMap.forEach((slotId, trinketInv) -> {
                        dev.emi.trinkets.api.SlotType slotType = slotGroup.getSlots().get(slotId);
                        if (slotType != null) {
                            for (int i = 0; i < trinketInv.size(); i++) {
                                this.addSlot(new SurvivalTrinketSlot(trinketInv, i, -9999, -9999, slotGroup, slotType, i, true));
                            }
                        }
                    });
                }
            });
        });
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Динамический расчет (броня всегда последние 4 слота инвентаря игрока)
            int hotbarStart = 27;
            int hotbarEnd = 36;
            int armorStart = 36;
            int armorEnd = 40;

            if (invSlot < armorStart) {
                if (originalStack.getItem() instanceof net.minecraft.item.ArmorItem) {
                    if (!this.insertItem(originalStack, armorStart, armorEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (invSlot < hotbarStart) {
                    if (!this.insertItem(originalStack, hotbarStart, hotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (invSlot >= hotbarStart && invSlot < hotbarEnd) {
                    if (!this.insertItem(originalStack, 0, hotbarStart, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (!this.insertItem(originalStack, 0, armorStart, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, originalStack);
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}