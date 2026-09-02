package com.conquest.hud.core.inventory;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import java.util.Map;

public class ConquestScreenHandler extends ScreenHandler {
    public static ScreenHandlerType<ConquestScreenHandler> TYPE;

    public ConquestScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(TYPE, syncId);

        // Инвентарь (0-26)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, -9999, -9999));
            }
        }
        // Хотбар (27-35)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, -9999, -9999));
        }

        // Ванильная броня (36-39) - Скрыта, используем только Trinkets
        final EquipmentSlot[] EQUIPMENT = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot equipSlot = EQUIPMENT[i];
            this.addSlot(new Slot(playerInventory, 39 - i, -9999, -9999) {
                @Override public int getMaxItemCount() { return 1; }
                @Override public boolean canInsert(ItemStack stack) { return stack.getItem() instanceof ArmorItem && ((ArmorItem) stack.getItem()).getSlotType() == equipSlot; }
            });
        }

    }

    @Override
    public void onSlotClick(int slotIndex, int button, net.minecraft.screen.slot.SlotActionType actionType, net.minecraft.entity.player.PlayerEntity player) {
        // Жесткая блокировка взаимодействия с хотбаром (27-35) и ванильной броней (36-39)
        if ((slotIndex >= 27 && slotIndex <= 35) || (slotIndex >= 36 && slotIndex <= 39)) {
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    @Override
    public net.minecraft.item.ItemStack quickMove(net.minecraft.entity.player.PlayerEntity player, int invSlot) {
        net.minecraft.item.ItemStack newStack = net.minecraft.item.ItemStack.EMPTY;
        net.minecraft.screen.slot.Slot slot = this.slots.get(invSlot);

        if (invSlot >= 27 && invSlot <= 35) {
            return net.minecraft.item.ItemStack.EMPTY;
        }

        if (slot != null && slot.hasStack()) {
            net.minecraft.item.ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            int totalSlots = this.slots.size();

            if (invSlot < 27) {
                if (!this.insertItem(originalStack, 40, totalSlots, false)) {
                    return net.minecraft.item.ItemStack.EMPTY;
                }
            } else if (invSlot >= 40) {
                if (!this.insertItem(originalStack, 0, 27, false)) {
                    return net.minecraft.item.ItemStack.EMPTY;
                }
            } else {
                return net.minecraft.item.ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(net.minecraft.item.ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return net.minecraft.item.ItemStack.EMPTY;
            }
            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }
}