package com.conquest.hud.core.stats;

import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class BackpackManager {

    public static int getMaxSlots(PlayerEntity player) {
        return 100; // Фиксированный размер, рисуем всегда всё
    }

    public static float getBackpackWeightBonus(PlayerEntity player) {
        var containers = ContainerComponentRegistry.CONTAINERS.getNullable(player);
        if (containers == null) return 0f;

        ItemStack backpack = containers.getEquipment().getStack(EquipmentSlot.BACKPACK.getIndex());
        if (backpack.isEmpty()) return 0f;

        String id = Registries.ITEM.getId(backpack.getItem()).toString();
        return switch (id) {
            case "conquest:backpack_lvl4" -> 60.0f;
            case "conquest:backpack_lvl3" -> 40.0f;
            case "conquest:backpack_lvl2" -> 30.0f;
            case "conquest:backpack_lvl1", "minecraft:leather_chestplate" -> 20.0f;
            default -> 0.0f;
        };
    }
}