package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IStaminaComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            IStaminaComponent stamina = StatsComponentRegistry.STAMINA.getNullable(player);
            if (stamina == null) return;

            float currentWeight = WeightManager.getCurrentWeight(player);
            float maxWeight = WeightManager.getMaxWeight(player);
            float jumpCost = 20.0f;

            if (currentWeight > maxWeight) {
                jumpCost += ((currentWeight - maxWeight) * 0.5f);
            }

            // Блокируем прыжок на обеих сторонах (клиент и сервер)
            if (stamina.getStamina() < jumpCost) {
                ci.cancel();
                return;
            }

            // Тратим стамину только на сервере во избежание двойного списания
            if (!player.getWorld().isClient()) {
                stamina.setStamina(stamina.getStamina() - jumpCost);
            }
        }
    }
}