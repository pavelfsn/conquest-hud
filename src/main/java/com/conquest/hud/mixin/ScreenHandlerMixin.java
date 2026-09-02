package com.conquest.hud.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        // Блокировка подмены предмета в руке через хоткей (нажатие цифр 1-9) при наведении на предмет
        if (actionType == SlotActionType.SWAP && button == player.getInventory().selectedSlot) {
            ci.cancel();
            return;
        }

        ScreenHandler handler = (ScreenHandler) (Object) this;
        if (slotIndex >= 0 && slotIndex < handler.slots.size()) {
            Slot slot = handler.slots.get(slotIndex);

            // Проверяем, принадлежит ли слот ванильному инвентарю игрока
            if (slot.inventory instanceof PlayerInventory) {
                int invIndex = slot.getIndex();

                // 36-39: Ванильная броня
                // 40: Вторая рука (Offhand)
                // selectedSlot: Активный слот хотбара (заблокирован на 0)
                if ((invIndex >= 36 && invIndex <= 40) || invIndex == player.getInventory().selectedSlot) {
                    ci.cancel();
                }
            }
        }
    }
}