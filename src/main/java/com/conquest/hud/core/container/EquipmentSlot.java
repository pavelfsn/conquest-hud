package com.conquest.hud.core.container;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public enum EquipmentSlot {
    HEAD(0, "head"),
    MASK(1, "mask"),
    TORSO(2, "torso"),        // раньше был 3, сделаем 2 для компактности
    ARMOR(3, "armor"),        // бронежилет
    RIG(4, "rig"),
    PANTS(5, "pants"),
    BOOTS(6, "boots"),
    BACKPACK(7, "backpack"),
    PRIMARY_WEAPON(8, "primary_weapon"),
    SECONDARY_WEAPON(9, "secondary_weapon"),
    // 6 слотов для аксессуаров (индексы 14–19)
    ACCESSORY_1(14, "accessory"),
    ACCESSORY_2(15, "accessory"),
    ACCESSORY_3(16, "accessory"),
    ACCESSORY_4(17, "accessory"),
    ACCESSORY_5(18, "accessory"),
    ACCESSORY_6(19, "accessory");

    private final int index;
    private final TagKey<Item> tagKey;

    EquipmentSlot(int index, String tagName) {
        this.index = index;
        this.tagKey = TagKey.of(RegistryKeys.ITEM, new Identifier("conquest", tagName));
    }

    public int getIndex() {
        return index;
    }

    public TagKey<Item> getTagKey() {
        return tagKey;
    }

    public static EquipmentSlot getByIndex(int index) {
        for (EquipmentSlot slot : values()) {
            if (slot.getIndex() == index) return slot;
        }
        return null;
    }
}