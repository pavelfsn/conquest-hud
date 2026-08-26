package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IPlayerStats;
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

        // Отсекаем мобов и выполнение на клиенте
        if (entity.getWorld().isClient() || !(entity instanceof PlayerEntity player)) {
            return originalHeal;
        }

        IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
        int metabolism = stats.getMetabolism();

        if (metabolism > 0) {
            // Формула: +2% к эффективности лечения за каждый поинт Метаболизма
            float multiplier = 1.0f + (metabolism * 0.02f);
            return originalHeal * multiplier;
        }

        return originalHeal;
    }
}