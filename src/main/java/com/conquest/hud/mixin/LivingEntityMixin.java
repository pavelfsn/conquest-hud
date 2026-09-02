package com.conquest.hud.mixin;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
    private float modifyHealAmount(float originalHeal) {
        if (originalHeal <= 0) return originalHeal;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getWorld().isClient() || !(entity instanceof PlayerEntity player)) return originalHeal;

        IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(player);
        if (progression == null) return originalHeal;

        int metabolism = progression.getStat(2);
        if (metabolism > 0) {
            return originalHeal * (1.0f + (metabolism * 0.03f)); // До +30%
        }
        return originalHeal;
    }
}