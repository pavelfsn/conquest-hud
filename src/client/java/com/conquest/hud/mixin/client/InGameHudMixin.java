package com.conquest.hud.mixin.client;

import com.conquest.hud.core.logger.ModLogger;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
    private void hideStatusBars(DrawContext context, CallbackInfo ci) {
        // Отключаем ванильные сердца, броню, сытость и воздух
        ci.cancel();
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void hideExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        // Отключаем ванильную полосу опыта
        ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void hideHotbar(float tickDelta, DrawContext context, CallbackInfo ci) {
        // Отключаем ванильный хотбар
        ci.cancel();
    }
}