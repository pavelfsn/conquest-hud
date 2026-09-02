package com.conquest.hud.mixin;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerTickMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(player);
        int agility = progression != null ? progression.getStat(1) : 0;
        int metabolism = progression != null ? progression.getStat(2) : 0;

        // 1. Стамина
        IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.getNullable(player);
        if (staminaComp != null) {
            float currentStamina = staminaComp.getStamina();
            float newStamina = currentStamina;

            float ratio = WeightManager.getCurrentWeight(player) / WeightManager.getMaxWeight(player);
            float regenPenalty = 0.0f;
            if (ratio >= 1.10f) regenPenalty = 0.25f;
            else if (ratio >= 1.00f) regenPenalty = 0.10f;

            if (player.isSprinting()) {
                newStamina -= 0.5f; // -10/сек
            } else {
                // База 15/сек (0.75 за тик). Макс 30/сек (1.5 за тик)
                float tickRegen = 0.75f + (agility * 0.075f);
                newStamina += (tickRegen * (1.0f - regenPenalty));
            }

            float maxStamina = staminaComp.getMaxStamina();
            newStamina = Math.max(0, Math.min(newStamina, maxStamina));

            if (Math.abs(currentStamina - newStamina) > 0.001f) {
                staminaComp.setStamina(newStamina);
            }
        }

        // 2. Метаболизм
        EntityAttributeInstance hpAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (hpAttr != null) {
            double targetHp = 200.0 + (metabolism * 10.0); // 200 - 300
            if (hpAttr.getBaseValue() != targetHp) hpAttr.setBaseValue(targetHp);
        }

        // Реген: 4 ХП/сек на 10 уровне = 0.4 ХП/сек за уровень.
        if (player.age % 20 == 0 && metabolism > 0 && player.getHealth() < player.getMaxHealth() && player.getHealth() > 0) {
            player.heal(metabolism * 0.4f);
        }
    }
}