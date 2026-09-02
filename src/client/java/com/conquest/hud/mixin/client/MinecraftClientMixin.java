package com.conquest.hud.mixin.client;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof InventoryScreen) {
            ConquestInventoryScreen conquestScreen = new ConquestInventoryScreen();
            MinecraftClient.getInstance().setScreen(conquestScreen);
            conquestScreen.setInitialVisibility(true, false);
            ci.cancel();
        }
    }
}