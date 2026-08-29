package com.conquest.hud.mixin.client;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    @Inject(method = "unpressAll", at = @At("HEAD"), cancellable = true)
    private static void onUnpressAll(CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ConquestInventoryScreen) {
            ci.cancel(); // Блокируем сброс клавиш движения при открытии нашего инвентаря
        }
    }
}