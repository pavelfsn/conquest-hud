package com.conquest.hud.core.stats;

import com.conquest.hud.core.config.ItemWeightConfig;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WeightManager {
    private static final UUID OVERWEIGHT_MODIFIER_ID = UUID.fromString("72b5f632-1111-4444-9999-abcdef123456");
    private static final Map<UUID, Float> clientCache = new HashMap<>();

    public static float getMaxWeight(PlayerEntity player) {
        IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
        if (stats == null) return 40.0f;
        return 40.0f + (stats.getStrength() * 2.0f);
    }

    public static float getCurrentWeight(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            return clientCache.getOrDefault(player.getUuid(), 0f);
        }
        IPlayerWeightComponent weightComp = StatsComponentRegistry.WEIGHT.get(player);
        return weightComp != null ? weightComp.getCurrentWeight() : 0f;
    }

    public static boolean canSprint(PlayerEntity player) {
        return getCurrentWeight(player) <= getMaxWeight(player);
    }

    public static void updateClientWeight(UUID playerId, float weight) {
        clientCache.put(playerId, weight);
    }

    public static void updateServerWeight(PlayerEntity player) {
        if (player.getWorld().isClient()) return;

        float[] totalWeight = {0.0f};

        for (int i = 0; i < player.getInventory().size(); i++) {
            var stack = player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                totalWeight[0] += ItemWeightConfig.getWeight(stack) * stack.getCount();
            }
        }

        Optional<TrinketComponent> trinkets = TrinketsApi.getTrinketComponent(player);
        if (trinkets.isPresent()) {
            trinkets.get().getAllEquipped().forEach(pair -> {
                var stack = pair.getRight();
                if (!stack.isEmpty()) {
                    totalWeight[0] += ItemWeightConfig.getWeight(stack) * stack.getCount();
                }
            });
        }

        IPlayerWeightComponent weightComp = StatsComponentRegistry.WEIGHT.get(player);
        if (weightComp == null) return;

        float roundedWeight = Math.round(totalWeight[0] * 10.0f) / 10.0f;
        float oldWeight = weightComp.getCurrentWeight();

        if (Math.abs(oldWeight - roundedWeight) > 0.001f) {
            weightComp.setCurrentWeight(roundedWeight);
            StatsComponentRegistry.WEIGHT.sync(player);
        }

        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(OVERWEIGHT_MODIFIER_ID);
            float maxWeight = getMaxWeight(player);
            if (roundedWeight > maxWeight) {
                float overweightRatio = (roundedWeight - maxWeight) / maxWeight;
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