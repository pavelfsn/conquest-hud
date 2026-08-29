package com.conquest.hud.mixin.client;

import com.conquest.hud.ConquestHud;
import com.conquest.hud.client.ConquestHudClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientCreativeMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetCreativeScreen(Screen screen, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (screen instanceof CreativeInventoryScreen && ConquestHudClient.isNanoVGReady()) {
            // Разрешаем ванильный креатив-экран, если у игрока включен режим креатива
            if (client.interactionManager != null && client.interactionManager.getCurrentGameMode().isCreative()) {
                return;
            }
            ci.cancel();
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    ConquestHud.OPEN_UI_PACKET,
                    net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create()
            );
        }
    }
}