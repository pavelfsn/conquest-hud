package com.conquest.hud.mixin.pointblank;

import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import com.conquest.hud.core.network.ContainerSlotUpdatePacket;
import com.vicmatskiv.pointblank.item.AmmoItem;
import com.vicmatskiv.pointblank.item.FireModeInstance;
import com.vicmatskiv.pointblank.item.GunItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GunItem.class, remap = false)
public abstract class GunItemAmmoMixin {

    @Shadow public abstract int getMaxAmmoCapacity(ItemStack itemStack, FireModeInstance fireModeInstance);

    @Inject(method = "canReloadGun", at = @At("HEAD"), cancellable = true, remap = false)
    private void customCanReloadGun(ItemStack gunStack, PlayerEntity player, FireModeInstance fireMode, CallbackInfoReturnable<Integer> cir) {
        GunItem gunItem = (GunItem) gunStack.getItem();
        int maxCapacity = this.getMaxAmmoCapacity(gunStack, fireMode);
        int currentAmmo = GunItem.getAmmo(gunStack, fireMode);
        int ammoNeeded = maxCapacity - currentAmmo;

        if (ammoNeeded <= 0) {
            cir.setReturnValue(0);
            return;
        }
        if (player.isCreative()) {
            cir.setReturnValue(ammoNeeded);
            return;
        }

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        if (containers == null) return;

        int availableBullets = countCompatibleAmmo(containers.getInventory(), gunItem, fireMode)
                + countCompatibleAmmo(containers.getHotbar(), gunItem, fireMode);

        cir.setReturnValue(Math.min(ammoNeeded, availableBullets));
    }

    @Inject(method = "reloadGun", at = @At("HEAD"), cancellable = true, remap = false)
    private void customReloadGun(ItemStack gunStack, PlayerEntity player, FireModeInstance fireMode, CallbackInfoReturnable<Integer> cir) {
        GunItem gunItem = (GunItem) gunStack.getItem();
        int maxCapacity = this.getMaxAmmoCapacity(gunStack, fireMode);
        int currentAmmo = GunItem.getAmmo(gunStack, fireMode);
        int neededAmmo = maxCapacity - currentAmmo;

        if (neededAmmo <= 0) {
            cir.setReturnValue(currentAmmo);
            return;
        }
        if (player.isCreative()) {
            int newAmmo = currentAmmo + neededAmmo;
            GunItem.setAmmo(gunStack, fireMode, newAmmo);
            cir.setReturnValue(newAmmo);
            return;
        }

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        if (containers == null) return;

        int foundAmmoCount = consumeAmmoFromContainer(player, containers.getInventory(), 0, gunItem, fireMode, neededAmmo);
        neededAmmo -= foundAmmoCount;

        if (neededAmmo > 0) {
            foundAmmoCount += consumeAmmoFromContainer(player, containers.getHotbar(), 2, gunItem, fireMode, neededAmmo);
        }

        int newAmmo = currentAmmo + foundAmmoCount;
        GunItem.setAmmo(gunStack, fireMode, newAmmo);
        cir.setReturnValue(newAmmo);
    }

    private int countCompatibleAmmo(IContainer container, GunItem gunItem, FireModeInstance fireMode) {
        int count = 0;
        for (int i = 0; i < container.getSize(); i++) {
            ItemStack stack = container.getStack(i);
            if (!stack.isEmpty() && isBulletCompatible(stack.getItem(), gunItem, fireMode)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int consumeAmmoFromContainer(PlayerEntity player, IContainer container, int containerId, GunItem gunItem, FireModeInstance fireMode, int neededAmmo) {
        int consumed = 0;
        for (int i = 0; i < container.getSize(); i++) {
            ItemStack stack = container.getStack(i);
            if (!stack.isEmpty() && isBulletCompatible(stack.getItem(), gunItem, fireMode)) {
                int available = stack.getCount();
                if (available <= neededAmmo) {
                    consumed += available;
                    neededAmmo -= available;
                    container.setStack(i, ItemStack.EMPTY);
                } else {
                    stack.decrement(neededAmmo);
                    consumed += neededAmmo;
                    neededAmmo = 0;
                }

                if (player instanceof ServerPlayerEntity sPlayer) {
                    ContainerSlotUpdatePacket.send(sPlayer, containerId, i, container.getStack(i));
                }

                if (neededAmmo == 0) break;
            }
        }
        return consumed;
    }

    private boolean isBulletCompatible(Item ammoItem, GunItem gunItem, FireModeInstance fireMode) {
        if (fireMode.isUsingDefaultAmmoPool()) {
            return gunItem.getCompatibleAmmo().contains(ammoItem);
        }
        return fireMode.getAmmo() == ammoItem;
    }
}