package com.conquest.hud.core.container;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class EquipmentSlotRegistry {
    private static final Map<Integer, String> SLOT_TAGS = new HashMap<>();

    static {
        // Автоматически заполняем из enum
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            SLOT_TAGS.put(slot.getIndex(), slot.getTagKey().id().toString());
        }
    }

    public static String getTagForSlot(int slotIndex) {
        return SLOT_TAGS.get(slotIndex);
    }

    public static boolean canEquip(ItemStack stack, int slotIndex) {
        String tagId = getTagForSlot(slotIndex);
        if (tagId == null || stack.isEmpty()) return false;
        TagKey<net.minecraft.item.Item> tag = TagKey.of(Registries.ITEM.getKey(), new Identifier(tagId));
        return stack.isIn(tag);
    }
}