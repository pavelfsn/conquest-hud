package com.conquest.hud.mixin.client;

import com.conquest.hud.ConquestHud;
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
        if (screen instanceof CreativeInventoryScreen) {
            // Проверяем, есть ли у игрока операторские права (OP / уровень 2+)
            boolean isOp = client.player != null && client.player.hasPermissionLevel(2);

            if (!isOp) {
                ci.cancel();
                // Отправляем пакет на открытие нашего кастомного инвентаря вместо креатива
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                        ConquestHud.OPEN_UI_PACKET,
                        net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create()
                );
            }
        }
    }
}