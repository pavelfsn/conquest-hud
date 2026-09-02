package com.conquest.hud.core.container;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class PlayerEquipmentContainer extends ItemContainer {

    public PlayerEquipmentContainer(int containerId, int size, float weightLimit) {
        super(containerId, size, weightLimit);
    }

    @Override
    public boolean canInsert(int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }

        EquipmentSlot slot = EquipmentSlot.getByIndex(slotIndex);
        if (slot == null) {
            return false;
        }

        // Временный хак для тестирования рюкзака без создания JSON-тегов
        if (slot == EquipmentSlot.BACKPACK && stack.getItem() == Items.LEATHER_CHESTPLATE) {
            return true;
        }

        return stack.isIn(slot.getTagKey());
    }
}