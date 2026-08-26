package com.conquest.hud.core.inventory;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;

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
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            // Индексы слотов:
            // 0-26: Рюкзак
            // 27-35: Хотбар
            // 36-39: Экипировка (Броня)

            if (invSlot < 36) { // Если клик по рюкзаку или хотбару
                // Пытаемся надеть броню
                if (originalStack.getItem() instanceof net.minecraft.item.ArmorItem) {
                    if (!this.insertItem(originalStack, 36, 40, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Иначе перекидываем между рюкзаком и хотбаром
                else if (invSlot < 27) {
                    if (!this.insertItem(originalStack, 27, 36, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (invSlot >= 27 && invSlot < 36) {
                    if (!this.insertItem(originalStack, 0, 27, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else { // Если клик по броне (снимаем её)
                if (!this.insertItem(originalStack, 0, 36, false)) {
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