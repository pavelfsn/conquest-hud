package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
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

        if (player.age % 10 == 0) {
            WeightManager.updateWeightEffects(player);
        }

        IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.get(player);
        float currentStamina = staminaComp.getStamina();
        float newStamina = currentStamina;

        if (player.isSprinting()) {
            newStamina -= 0.5f;
        } else {
            newStamina += 0.3f;
        }

        if (currentStamina != newStamina) {
            ((StaminaComponent) staminaComp).setStaminaRaw(newStamina);
            if ((int) currentStamina != (int) newStamina) {
                StatsComponentRegistry.STAMINA.sync(player);
            }
        }
    }
}