package com.conquest.hud.mixin.client.pointblank;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.vicmatskiv.pointblank.feature.RecoilFeature;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RecoilFeature.class, remap = false)
public class RecoilFeatureMixin {

    @Inject(method = "getRecoilModifier(Lnet/minecraft/item/ItemStack;)F", at = @At("RETURN"), cancellable = true, remap = false)
    private static void applyRecoilSkill(ItemStack itemStack, CallbackInfoReturnable<Float> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player != null) {
            IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (prog != null) {
                int controlSkill = prog.getWeaponSkill(2);
                float originalRecoil = cir.getReturnValue();
                float newRecoil = originalRecoil * (1.0f - (controlSkill * 0.03f));
                cir.setReturnValue(Math.max(0.01f, newRecoil));
            }
        }
    }
}