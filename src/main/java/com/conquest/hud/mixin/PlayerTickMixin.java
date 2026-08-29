package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
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

        IStaminaComponent staminaComp = StatsComponentRegistry.STAMINA.get(player);
        if (staminaComp == null) return;

        float currentStamina = staminaComp.getStamina();
        float newStamina = currentStamina;

        if (player.isSprinting()) {
            newStamina -= 0.5f;
        } else {
            newStamina += 0.3f;
        }

        newStamina = Math.max(0, Math.min(newStamina, staminaComp.getMaxStamina()));

        if (Math.abs(currentStamina - newStamina) > 0.001f) {
            staminaComp.setStamina(newStamina);
        }
    }
}