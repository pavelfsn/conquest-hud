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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Unique
    private int conquestSweepTick = 0;

    // ПЕРЕХВАТ ВАНИЛЬНОГО ОПЫТА ОТ УБИЙСТВА МОБОВ И РУД
    @Inject(method = "addExperience", at = @At("HEAD"), cancellable = true)
    private void onAddExperience(int experience, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.getWorld().isClient() && experience > 0) {
            com.conquest.hud.core.progression.IProgressionComponent prog = com.conquest.hud.core.stats.StatsComponentRegistry.PROGRESSION.getNullable(player);
            if (prog != null) {
                prog.addXp(experience * 5000L); // Базово зомби даст ~25 000 XP
            }
        }
        ci.cancel(); // Отключаем ванильную зеленую полоску
    }

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    private void onDropInventory(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        if (containers != null) {
            IContainer inv = containers.getInventory();
            List<Integer> validSlots = new ArrayList<>();

            for (int i = 0; i < inv.getSize(); i++) {
                if (!inv.getStack(i).isEmpty()) {
                    validSlots.add(i);
                }
            }

            Collections.shuffle(validSlots);
            int slotsToCheck = Math.min(30, validSlots.size());

            com.conquest.hud.core.progression.IProgressionComponent progression = com.conquest.hud.core.stats.StatsComponentRegistry.PROGRESSION.getNullable(player);
            int luck = progression != null ? progression.getStat(3) : 0; // 3 = Удача
            float dropChance = 0.60f - (luck * 0.02f);

            for (int i = 0; i < slotsToCheck; i++) {
                int slotIndex = validSlots.get(i);
                ItemStack stack = inv.getStack(slotIndex);

                if (player.getWorld().random.nextFloat() <= dropChance) {
                    int loseCount = Math.max(2, (int) (stack.getCount() * 0.05f));
                    if (loseCount >= stack.getCount()) {
                        player.dropItem(stack.copy(), true, false);
                        inv.setStack(slotIndex, ItemStack.EMPTY);
                    } else {
                        ItemStack droppedStack = stack.split(loseCount);
                        player.dropItem(droppedStack, true, false);
                    }
                }
            }
        }
        player.getInventory().clear();
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickSweep(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.getWorld().isClient()) return;

        conquestSweepTick++;
        if (conquestSweepTick % 10 != 0) return;

        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        if (containers == null) return;

        IContainer customInv = containers.getInventory();
        PlayerInventory vanillaInv = player.getInventory();
        boolean changed = false;

        int maxSlots = BackpackManager.getMaxSlots(player);

        for (int i = 0; i < vanillaInv.main.size(); i++) {
            if (i == vanillaInv.selectedSlot) continue;

            ItemStack stack = vanillaInv.main.get(i);
            if (!stack.isEmpty()) {
                ItemStack remainder = insertIntoCustom((ServerPlayerEntity) player, customInv, stack, maxSlots);
                vanillaInv.main.set(i, remainder);
                if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
                    changed = true;
                }
            }
        }

        if (changed) {
            WeightManager.updateServerWeight(player);
        }
    }

    @Unique
    private ItemStack insertIntoCustom(ServerPlayerEntity player, IContainer customInv, ItemStack stack, int maxSlots) {
        ItemStack copy = stack.copy();
        for (int i = 0; i < maxSlots; i++) {
            ItemStack slotStack = customInv.getStack(i);
            if (!slotStack.isEmpty() && ItemStack.canCombine(slotStack, copy)) {
                int space = slotStack.getMaxCount() - slotStack.getCount();
                int move = Math.min(space, copy.getCount());
                if (move > 0) {
                    slotStack.increment(move);
                    copy.decrement(move);
                    ContainerSlotUpdatePacket.send(player, 0, i, slotStack);
                    if (copy.isEmpty()) return ItemStack.EMPTY;
                }
            }
        }
        for (int i = 0; i < maxSlots; i++) {
            if (customInv.getStack(i).isEmpty()) {
                customInv.setStack(i, copy.copy());
                ContainerSlotUpdatePacket.send(player, 0, i, customInv.getStack(i));
                return ItemStack.EMPTY;
            }
        }
        return copy;
    }
}