package com.conquest.hud.mixin.client;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void interceptHotbarInput(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player == null) return;

        // Жестко отключаем ванильную обработку клавиш 1-9
        for (int i = 0; i < 9; i++) {
            while (client.options.hotbarKeys[i].wasPressed()) {
                // Сжигаем нажатия, чтобы ванильный код на них не реагировал
            }
        }
    }
}