package com.conquest.hud.core.stats;

import com.conquest.hud.core.config.ItemWeightConfig;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class WeightManager {
    private static final UUID OVERWEIGHT_MODIFIER_ID = UUID.fromString("72b5f632-1111-4444-9999-abcdef123456");

    public static float getMaxWeight(PlayerEntity player) {
        IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
        // Базовый вес 40 кг + 2 кг за каждое очко Силы
        return 40.0f + (stats.getStrength() * 2.0f);
    }

    public static float getCurrentWeight(PlayerEntity player) {
        // И клиент, и сервер читают значение из компонента CCA
        return StatsComponentRegistry.WEIGHT.get(player).getCurrentWeight();
    }

    public static boolean canSprint(PlayerEntity player) {
        return getCurrentWeight(player) <= getMaxWeight(player);
    }

    // Выполняется строго на сервере (например, в PlayerTickMixin)
    public static void updateServerWeight(PlayerEntity player) {
        if (player.getWorld().isClient()) return;

        float totalWeight = 0.0f;
        for (int i = 0; i < player.getInventory().size(); i++) {
            net.minecraft.item.ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                totalWeight += ItemWeightConfig.getWeight(stack) * stack.getCount();
            }
        }

        IPlayerWeightComponent weightComp = StatsComponentRegistry.WEIGHT.get(player);
        float oldWeight = weightComp.getCurrentWeight();

        // Округляем до десятых, чтобы не спамить сетевыми пакетами каждый тик
        float roundedWeight = Math.round(totalWeight * 10.0f) / 10.0f;

        if (oldWeight != roundedWeight) {
            weightComp.setCurrentWeight(roundedWeight);
            StatsComponentRegistry.WEIGHT.sync(player);
        }

        // Применение штрафов к скорости перемещения
        float maxWeight = getMaxWeight(player);
        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);

        if (speedAttr != null) {
            speedAttr.removeModifier(OVERWEIGHT_MODIFIER_ID);

            if (roundedWeight > maxWeight) {
                float overweightRatio = (roundedWeight - maxWeight) / maxWeight;
                // Снижение скорости максимум на 60%
                float penalty = Math.min(overweightRatio * 0.5f, 0.6f);

                EntityAttributeModifier modifier = new EntityAttributeModifier(
                        OVERWEIGHT_MODIFIER_ID,
                        "Overweight Slowdown",
                        -penalty,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttr.addTemporaryModifier(modifier);
            }
        }
    }
}