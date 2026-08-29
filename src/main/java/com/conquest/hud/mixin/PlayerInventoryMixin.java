package com.conquest.hud.mixin;

import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow @Final public DefaultedList<ItemStack> main;
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "getEmptySlot", at = @At("HEAD"), cancellable = true)
    private void onGetEmptySlot(CallbackInfoReturnable<Integer> cir) {
        for (int i = 9; i < 36; ++i) {
            if (this.main.get(i).isEmpty()) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    @Inject(method = "getOccupiedSlotWithRoomForStack", at = @At("HEAD"), cancellable = true)
    private void onGetOccupiedSlotWithRoomForStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        PlayerInventory inv = (PlayerInventory) (Object) this;
        for (int i = 9; i < 36; ++i) {
            ItemStack current = this.main.get(i);
            if (!current.isEmpty() && ItemStack.canCombine(stack, current) && current.isStackable() && current.getCount() < current.getMaxCount() && current.getCount() < inv.getMaxCountPerStack()) {
                cir.setReturnValue(i);
                return;
            }
        }
        cir.setReturnValue(-1);
    }

    // Мгновенный пересчет веса при любом изменении инвентаря
    @Inject(method = "markDirty", at = @At("TAIL"))
    private void onMarkDirty(CallbackInfo ci) {
        if (this.player != null && !this.player.getWorld().isClient()) {
            WeightManager.updateServerWeight(this.player);
        }
    }
}