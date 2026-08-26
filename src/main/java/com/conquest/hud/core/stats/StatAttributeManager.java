package com.conquest.hud.core.stats;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public class StatAttributeManager {
    private static final UUID VITALITY_MODIFIER_ID = UUID.fromString("c0700000-0000-0000-0000-000000000001");
    private static final UUID AGILITY_MODIFIER_ID = UUID.fromString("a9100000-0000-0000-0000-000000000001");

    public static void updateAttributes(PlayerEntity player, IPlayerStats stats) {
        if (player.getWorld().isClient()) return;

        EntityAttributeInstance healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(VITALITY_MODIFIER_ID);
            if (stats.getVitality() > 0) {
                healthAttr.addPersistentModifier(new EntityAttributeModifier(
                        VITALITY_MODIFIER_ID, "RPG Vitality Bonus",
                        stats.getVitality() * 1.0, EntityAttributeModifier.Operation.ADDITION));
            }
        }

        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(AGILITY_MODIFIER_ID);
            if (stats.getAgility() > 0) {
                speedAttr.addPersistentModifier(new EntityAttributeModifier(
                        AGILITY_MODIFIER_ID, "RPG Agility Bonus",
                        stats.getAgility() * 0.01, EntityAttributeModifier.Operation.MULTIPLY_BASE));
            }
        }
    }
}