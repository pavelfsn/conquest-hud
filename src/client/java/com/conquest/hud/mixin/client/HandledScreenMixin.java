package com.conquest.hud.mixin.client;

import com.conquest.hud.client.gui.ConquestInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    @Shadow protected abstract boolean isPointWithinBounds(int x, int y, int width, int height, double pointX, double pointY);

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void onDrawSlotHead(DrawContext context, Slot slot, CallbackInfo ci) {
        if ((Object) this instanceof ConquestInventoryScreen) {
            context.getMatrices().push();
            context.getMatrices().translate(slot.x, slot.y, 0);
            context.getMatrices().scale(2.0f, 2.0f, 1.0f); // Изменено: масштаб 16 * 2 = 32
            context.getMatrices().translate(-slot.x, -slot.y, 0);
        }
    }

    @Inject(method = "drawSlot", at = @At("RETURN"))
    private void onDrawSlotReturn(DrawContext context, Slot slot, CallbackInfo ci) {
        if ((Object) this instanceof ConquestInventoryScreen) {
            context.getMatrices().pop();
        }
    }

    @Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
    private void onIsPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ConquestInventoryScreen) {
            // Изменено: хитбокс 32x32
            cir.setReturnValue(this.isPointWithinBounds(slot.x, slot.y, 32, 32, pointX, pointY));
        }
    }

    @Inject(method = "drawSlotHighlight", at = @At("HEAD"), cancellable = true)
    private static void onDrawSlotHighlight(DrawContext context, int x, int y, int z, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ConquestInventoryScreen) {
            // Изменено: размер заливки 32x32
            context.fillGradient(RenderLayer.getGuiOverlay(), x, y, x + 32, y + 32, 0x33FFFFFF, 0x33FFFFFF, z);
            ci.cancel();
        }
    }
}