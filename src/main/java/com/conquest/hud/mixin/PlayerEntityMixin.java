package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int modifyExperienceGain(int originalExperience) {
        if (originalExperience <= 0) return originalExperience;

        PlayerEntity player = (PlayerEntity) (Object) this;

        // Обработка только на стороне сервера
        if (player.getWorld().isClient()) return originalExperience;

        IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
        int intellect = stats.getIntellect();

        if (intellect > 0) {
            // Формула: +5% опыта за каждую единицу Интеллекта
            float multiplier = 1.0f + (intellect * 0.05f);
            return (int) Math.ceil(originalExperience * multiplier);
        }

        return originalExperience;
    }
}