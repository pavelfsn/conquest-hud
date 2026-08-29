package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerJumpMixin {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        IStaminaComponent stamina = StatsComponentRegistry.STAMINA.get(player);
        if (stamina == null) return;

        float currentWeight = WeightManager.getCurrentWeight(player);
        float maxWeight = WeightManager.getMaxWeight(player);
        float jumpCost = 10.0f;

        // Штраф за перевес
        if (currentWeight > maxWeight) {
            jumpCost += ((currentWeight - maxWeight) * 0.5f);
        }

        if (stamina.getStamina() < jumpCost) {
            ci.cancel();
            return;
        }

        stamina.setStamina(stamina.getStamina() - jumpCost);
    }
}