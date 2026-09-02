package com.conquest.hud.core.stats;

import com.conquest.hud.core.config.ItemWeightConfig;
import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.progression.IProgressionComponent;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import java.util.UUID;

public class WeightManager {
    private static final UUID OVERWEIGHT_MODIFIER_ID = UUID.fromString("72b5f632-1111-4444-9999-abcdef123456");
    private static final UUID AGILITY_MODIFIER_ID = UUID.fromString("83a6c721-2222-5555-8888-fedcba654321");

    public static float getItemWeight(ItemStack stack) {
        if (stack.isEmpty()) return 0f;
        return ItemWeightConfig.getWeight(stack);
    }

    public static float getMaxWeight(PlayerEntity player) {
        IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(player);
        float baseWeight = 40.0f;
        if (progression != null) baseWeight += (progression.getStat(0) * 3.0f);
        return baseWeight + BackpackManager.getBackpackWeightBonus(player);
    }

    public static float getCurrentWeight(PlayerEntity player) {
        IPlayerWeightComponent weightComp = StatsComponentRegistry.WEIGHT.getNullable(player);
        return weightComp != null ? weightComp.getCurrentWeight() : 0f;
    }

    public static boolean canSprint(PlayerEntity player) {
        return getCurrentWeight(player) <= getMaxWeight(player);
    }

    public static void updateServerWeight(PlayerEntity player) {
        if (player.getWorld().isClient()) return;
        float totalWeight = 0.0f;
        com.conquest.hud.core.container.IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.getNullable(player);

        if (containers != null) {
            totalWeight += containers.getInventory().getCurrentWeight();
            totalWeight += containers.getEquipment().getCurrentWeight();
        }

        IPlayerWeightComponent weightComp = StatsComponentRegistry.WEIGHT.getNullable(player);
        if (weightComp == null) return;

        float roundedWeight = Math.round(totalWeight * 10.0f) / 10.0f;
        if (Math.abs(weightComp.getCurrentWeight() - roundedWeight) > 0.001f) {
            weightComp.setCurrentWeight(roundedWeight);
            StatsComponentRegistry.WEIGHT.sync(player);
        }

        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(OVERWEIGHT_MODIFIER_ID);
            speedAttr.removeModifier(AGILITY_MODIFIER_ID);

            IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (progression != null) {
                int agility = progression.getStat(1);
                if (agility > 0) {
                    speedAttr.addTemporaryModifier(new EntityAttributeModifier(AGILITY_MODIFIER_ID, "Agility Speed", agility * 0.03, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            }

            float maxWeight = getMaxWeight(player);
            if (roundedWeight > maxWeight) {
                float overweightRatio = (roundedWeight - maxWeight) / maxWeight;
                float penalty = Math.min(overweightRatio * 0.5f, 0.6f);
                speedAttr.addTemporaryModifier(new EntityAttributeModifier(OVERWEIGHT_MODIFIER_ID, "Overweight Slowdown", -penalty, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }
}