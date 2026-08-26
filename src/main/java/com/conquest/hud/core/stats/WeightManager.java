package com.conquest.hud.core.stats;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class WeightManager {
    private static final UUID WEIGHT_PENALTY_ID = UUID.fromString("b8200000-0000-0000-0000-000000000002");
    private static final float ITEM_WEIGHT_STUB = 0.5f; // Заглушка веса за 1 предмет

    public static float getMaxWeight(PlayerEntity player) {
        IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
        return 40.0f + (stats.getStrength() * 4.0f);
    }

    public static float getCurrentWeight(PlayerEntity player) {
        float totalWeight = 0.0f;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                totalWeight += stack.getCount() * ITEM_WEIGHT_STUB;
            }
        }
        return totalWeight;
    }

    public static boolean canSprint(PlayerEntity player) {
        return (getCurrentWeight(player) - getMaxWeight(player)) <= 10.0f;
    }

    public static void updateWeightEffects(PlayerEntity player) {
        float maxWeight = getMaxWeight(player);
        float currentWeight = getCurrentWeight(player);
        float excess = currentWeight - maxWeight;

        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr == null) return;

        speedAttr.removeModifier(WEIGHT_PENALTY_ID);

        if (excess > 0) {
            double penalty;
            if (excess <= 10.0f) {
                penalty = -0.15; // Легкий перевес (15% штраф)
            } else if (excess <= 20.0f) {
                penalty = -0.40; // Сильный перевес (40% штраф)
            } else {
                penalty = -0.70; // Тяжелый перевес (70% штраф)
            }

            // MULTIPLY_TOTAL режет финальную скорость после применения всех бонусов Ловкости
            speedAttr.addPersistentModifier(new EntityAttributeModifier(
                    WEIGHT_PENALTY_ID, "Weight Penalty",
                    penalty, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }
}