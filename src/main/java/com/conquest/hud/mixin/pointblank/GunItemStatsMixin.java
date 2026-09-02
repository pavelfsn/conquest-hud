package com.conquest.hud.mixin.pointblank;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.vicmatskiv.pointblank.client.GunClientState;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GunItem.class, remap = false)
public class GunItemStatsMixin {

    @Inject(method = "getDrawCooldownDuration", at = @At("RETURN"), cancellable = true, remap = false)
    private void applyDrawSkill(LivingEntity entity, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Long> cir) {
        if (entity instanceof PlayerEntity player) {
            IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (prog != null) {
                int drawSkill = prog.getWeaponSkill(0);
                long originalTime = cir.getReturnValue();
                long newTime = (long) (originalTime * (1.0f - (drawSkill * 0.05f)));
                cir.setReturnValue(Math.max(0L, newTime));
            }
        }
    }

    @Inject(method = "getReloadingCooldownTime", at = @At("RETURN"), cancellable = true, remap = false)
    private void applyReloadSkill(GunItem.ReloadPhase phase, LivingEntity entity, GunClientState state, ItemStack itemStack, CallbackInfoReturnable<Long> cir) {
        if (entity instanceof PlayerEntity player) {
            IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (prog != null) {
                int reloadSkill = prog.getWeaponSkill(1);
                long originalTime = cir.getReturnValue();
                long newTime = (long) (originalTime * (1.0f - (reloadSkill * 0.04f)));
                cir.setReturnValue(Math.max(0L, newTime));
            }
        }
    }
}