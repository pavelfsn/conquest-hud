package com.conquest.hud.mixin;

import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import com.conquest.hud.core.network.ContainerSlotUpdatePacket;
import com.conquest.hud.core.stats.BackpackManager;
import com.conquest.hud.core.stats.WeightManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onInsertStack(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (player.getWorld().isClient()) return;

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        if (containers == null) return;

        IContainer inv = containers.getInventory();
        int maxSlots = BackpackManager.getMaxSlots(player);
        Set<Integer> updatedSlots = new HashSet<>();

        // 1. Сначала пытаемся стакать предметы
        for (int i = 0; i < maxSlots; i++) {
            ItemStack existing = inv.getStack(i);
            if (!existing.isEmpty() && ItemStack.canCombine(existing, stack)) {
                int space = existing.getMaxCount() - existing.getCount();
                int move = Math.min(space, stack.getCount());
                if (move > 0) {
                    existing.increment(move);
                    stack.decrement(move);
                    updatedSlots.add(i);
                    if (stack.isEmpty()) break;
                }
            }
        }

        // 2. Если предмет еще остался, кладем в пустой слот
        if (!stack.isEmpty()) {
            for (int i = 0; i < maxSlots; i++) {
                if (inv.getStack(i).isEmpty()) {
                    inv.setStack(i, stack.copy());
                    stack.setCount(0);
                    updatedSlots.add(i);
                    break;
                }
            }
        }

        if (!updatedSlots.isEmpty()) {
            // Отправляем точечные апдейты только для измененных слотов
            ServerPlayerEntity sPlayer = (ServerPlayerEntity) player;
            for (int slotId : updatedSlots) {
                ContainerSlotUpdatePacket.send(sPlayer, 0, slotId, inv.getStack(slotId));
            }

            WeightManager.updateServerWeight(player);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }
}