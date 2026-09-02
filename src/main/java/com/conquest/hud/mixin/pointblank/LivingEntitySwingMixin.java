package com.conquest.hud.mixin.pointblank;

import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySwingMixin {

    @Shadow public abstract ItemStack getStackInHand(Hand hand);

    @Inject(method = "swingHand(Lnet/minecraft/util/Hand;Z)V", at = @At("HEAD"), cancellable = true)
    private void stopGunSwing(Hand hand, boolean fromServerPlayer, CallbackInfo ci) {
        ItemStack stack = this.getStackInHand(hand);
        if (!stack.isEmpty() && stack.getItem() instanceof GunItem) {
            ci.cancel();
        }
    }
}