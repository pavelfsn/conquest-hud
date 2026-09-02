package com.conquest.hud.mixin.client;

import com.conquest.hud.client.gui.ContainerWindow;
import com.conquest.hud.client.network.ClientPacketSender;
import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.network.ContainerActionType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {
    private ContainerWindow conquestInventoryWindow;

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            if ((Object) this instanceof com.conquest.hud.client.gui.ConquestInventoryScreen) return;

            var containers = ContainerComponentRegistry.CONTAINERS.get(client.player);
            if (containers != null) {
                HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
                conquestInventoryWindow = new ContainerWindow("Инвентарь", screen.width - 380, (screen.height - 430) / 2, 370, 430, containers.getInventory(), 10, true);
            }
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderTail(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (conquestInventoryWindow != null && conquestInventoryWindow.isVisible()) {

            com.conquest.hud.client.render.nanovg.NanoVGManager.setupAndDraw(() -> {
                conquestInventoryWindow.renderNanoVGBackground(mouseX, mouseY);
            });

            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 300); // Рисуем кастомный инвентарь высоко поверх ванильного
            conquestInventoryWindow.renderForeground(context, mouseX, mouseY, delta);
            context.getMatrices().pop();

            int hoveredSlot = conquestInventoryWindow.getHoveredSlot(mouseX, mouseY);
            if (hoveredSlot != -1) {
                MinecraftClient client = MinecraftClient.getInstance();
                ItemStack hoveredStack = conquestInventoryWindow.getContainer().getStack(hoveredSlot);
                if (!hoveredStack.isEmpty() && client.player != null) {
                    context.getMatrices().push();
                    context.getMatrices().translate(0, 0, 500);
                    context.drawTooltip(
                            client.textRenderer,
                            hoveredStack.getTooltip(client.player, client.options.advancedItemTooltips ? net.minecraft.client.item.TooltipContext.Default.ADVANCED : net.minecraft.client.item.TooltipContext.Default.BASIC),
                            hoveredStack.getTooltipData(),
                            mouseX,
                            mouseY
                    );
                    context.getMatrices().pop();
                }
            }
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (conquestInventoryWindow != null && conquestInventoryWindow.isVisible()) {
            if (slot.inventory instanceof PlayerInventory) ci.cancel();
        }
    }

    @Inject(method = "isPointOverSlot", at = @At("HEAD"), cancellable = true)
    private void onIsPointOverSlot(Slot slot, double pointX, double pointY, CallbackInfoReturnable<Boolean> cir) {
        if (conquestInventoryWindow != null && conquestInventoryWindow.isVisible()) {
            if (slot.inventory instanceof PlayerInventory) cir.setReturnValue(false);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClickedCustom(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (conquestInventoryWindow != null && conquestInventoryWindow.isVisible()) {
            MinecraftClient client = MinecraftClient.getInstance();

            // Если в руках предмет из сундука и мы кликнули по корзине нашего инвентаря
            if (button == 0 && client.player != null && !client.player.currentScreenHandler.getCursorStack().isEmpty()) {
                if (conquestInventoryWindow.isHoveredTrash(mouseX, mouseY)) {
                    // sourceId -2 означает удаление ванильного курсора
                    ClientPacketSender.sendAction(ContainerActionType.DELETE, -2, -1, -1, -1);
                    cir.setReturnValue(true);
                    return;
                }
            }

            if (conquestInventoryWindow.mouseClicked(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
                return;
            }
            int customSlot = conquestInventoryWindow.getHoveredSlot(mouseX, mouseY);
            if (customSlot != -1) {
                handleBridgeClick(customSlot, button);
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void onMouseReleasedCustom(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (conquestInventoryWindow != null && conquestInventoryWindow.isVisible()) {
            if (conquestInventoryWindow.mouseReleased(mouseX, mouseY, button)) {
                cir.setReturnValue(true);
            }
        }
    }

    private void handleBridgeClick(int slot, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
        if (!cursorStack.isEmpty()) {
            ClientPacketSender.sendAction(ContainerActionType.VANILLA_TO_CUSTOM, -1, -1, 0, slot);
        } else {
            ClientPacketSender.sendAction(ContainerActionType.CUSTOM_TO_VANILLA, 0, slot, -1, -1);
        }
    }
}