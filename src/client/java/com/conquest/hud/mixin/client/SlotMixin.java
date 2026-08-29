package com.conquest.hud.mixin.client;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Inject(method = "getBackgroundSprite", at = @At("HEAD"), cancellable = true)
    private void hideSlotBackgrounds(CallbackInfoReturnable<Pair<Identifier, Identifier>> cir) {
        if (MinecraftClient.getInstance().currentScreen instanceof ConquestInventoryScreen) {
            cir.setReturnValue(null); // Блокируем отрисовку любых фоновых иконок (ванильных и Trinkets)
        }
    }
}