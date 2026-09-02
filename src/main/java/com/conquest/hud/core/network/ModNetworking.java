package com.conquest.hud.core.network;

import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.EquipmentSlot;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import com.conquest.hud.core.container.ItemContainer;
import com.conquest.hud.core.stats.BackpackManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModNetworking {
    private static final Map<UUID, Integer> activeWeapons = new HashMap<>();

    public static void registerServerReceivers() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
            if (containers != null) {
                syncVanillaEquipment(player, containers.getEquipment());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ContainerActionPacket.ID, (server, player, handler, buf, responseSender) -> {
            ContainerActionType action = buf.readEnumConstant(ContainerActionType.class);
            int sourceContainerId = buf.readInt();
            int sourceSlot = buf.readInt();
            int targetContainerId = buf.readInt();
            int targetSlot = buf.readInt();

            server.execute(() -> processAction(player, action, sourceContainerId, sourceSlot, targetContainerId, targetSlot));
        });

        // Регистрация пакета прокачки
        ProgressionActionPacket.registerServerReceiver();
    }

    private static void processAction(ServerPlayerEntity player, ContainerActionType action, int sourceId, int sourceSlot, int targetId, int targetSlot) {
        IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
        IContainer sourceContainer = getContainer(containers, sourceId);

        boolean needsWeightSync = false;

        // ... (Код для DELETE -2 и VANILLA_TO_CUSTOM / CUSTOM_TO_VANILLA остается без изменений) ...
        if (action == ContainerActionType.DELETE && sourceId == -2) {
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            player.currentScreenHandler.updateToClient();
            return;
        }

        if (action == ContainerActionType.VANILLA_TO_CUSTOM) {
            ItemStack vanillaCursor = player.currentScreenHandler.getCursorStack();
            IContainer targetContainer = getContainer(containers, targetId);

            if (targetContainer != null && targetSlot >= 0 && targetSlot < targetContainer.getSize() && !vanillaCursor.isEmpty()) {
                ItemStack customTarget = targetContainer.getStack(targetSlot);

                if (customTarget.isEmpty()) {
                    targetContainer.setStack(targetSlot, vanillaCursor.copy());
                    player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
                    needsWeightSync = true;
                } else if (ItemStack.canCombine(vanillaCursor, customTarget)) {
                    int space = customTarget.getMaxCount() - customTarget.getCount();
                    int moveCount = Math.min(space, vanillaCursor.getCount());
                    if (moveCount > 0) {
                        customTarget.increment(moveCount);
                        vanillaCursor.decrement(moveCount);
                        needsWeightSync = true;
                    }
                } else {
                    targetContainer.setStack(targetSlot, vanillaCursor.copy());
                    player.currentScreenHandler.setCursorStack(customTarget.copy());
                    needsWeightSync = true;
                }

                if (needsWeightSync) {
                    ContainerSlotUpdatePacket.send(player, targetId, targetSlot, targetContainer.getStack(targetSlot));
                    player.currentScreenHandler.updateToClient();
                    com.conquest.hud.core.stats.WeightManager.updateServerWeight(player);
                }
            }
            return;
        }

        if (action == ContainerActionType.CUSTOM_TO_VANILLA) {
            ItemStack vanillaCursor = player.currentScreenHandler.getCursorStack();
            if (vanillaCursor.isEmpty() && sourceContainer != null && sourceSlot >= 0) {
                ItemStack customSource = sourceContainer.getStack(sourceSlot);
                if (!customSource.isEmpty()) {
                    player.currentScreenHandler.setCursorStack(customSource.copy());
                    sourceContainer.setStack(sourceSlot, ItemStack.EMPTY);

                    ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, ItemStack.EMPTY);
                    player.currentScreenHandler.updateToClient();
                    com.conquest.hud.core.stats.WeightManager.updateServerWeight(player);
                }
            }
            return;
        }

        if (sourceContainer == null) return;

        if (action == ContainerActionType.USE && sourceId == 0 && sourceSlot == -1) {
            if (sourceContainer instanceof ItemContainer itemContainer) {
                itemContainer.compactAndSort();
                ContainerSyncPacket.send(player, 0, itemContainer.getItems());
            }
            return;
        }

        if (sourceSlot >= 0 && sourceSlot >= sourceContainer.getSize()) return;

        int maxInvSlots = BackpackManager.getMaxSlots(player);

        if (targetId == 0 && targetSlot >= maxInvSlots) {
            ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, sourceContainer.getStack(sourceSlot));
            IContainer targetContainer = getContainer(containers, targetId);
            if (targetContainer != null) ContainerSlotUpdatePacket.send(player, targetId, targetSlot, targetContainer.getStack(targetSlot));
            return;
        }

        if (action == ContainerActionType.USE) {
            if (sourceId == 1) {
                activeWeapons.put(player.getUuid(), sourceSlot);
                syncVanillaEquipment(player, containers.getEquipment());
            } else if (sourceId == 0 || sourceId == 2) {
                ItemStack consumable = sourceContainer.getStack(sourceSlot);
                // ФИКС: Проверка canConsume, чтобы нельзя было спамить едой
                if (!consumable.isEmpty() && consumable.isFood() && player.canConsume(consumable.getItem().getFoodComponent().isAlwaysEdible())) {
                    player.eatFood(player.getWorld(), consumable.copy());
                    consumable.decrement(1);
                    ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, sourceContainer.getStack(sourceSlot));
                    needsWeightSync = true;
                }
            }
        } else {
            if (sourceSlot < 0) return;
            ItemStack sourceStack = sourceContainer.getStack(sourceSlot);
            if (sourceStack.isEmpty()) return;

            if (action == ContainerActionType.DELETE) {
                sourceContainer.setStack(sourceSlot, ItemStack.EMPTY);
                ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, ItemStack.EMPTY);
                needsWeightSync = true;
            } else if (action == ContainerActionType.MOVE) {
                IContainer targetContainer = getContainer(containers, targetId);
                if (targetContainer != null && targetSlot >= 0 && targetSlot < targetContainer.getSize()) {
                    ItemStack targetStack = targetContainer.getStack(targetSlot);

                    // ФИКС: Логика стаканья одинаковых предметов
                    if (!targetStack.isEmpty() && ItemStack.canCombine(sourceStack, targetStack)) {
                        int space = targetStack.getMaxCount() - targetStack.getCount();
                        int moveCount = Math.min(space, sourceStack.getCount());
                        if (moveCount > 0) {
                            targetStack.increment(moveCount);
                            sourceStack.decrement(moveCount);

                            ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, sourceContainer.getStack(sourceSlot));
                            ContainerSlotUpdatePacket.send(player, targetId, targetSlot, targetContainer.getStack(targetSlot));
                            needsWeightSync = true;
                        }
                    }
                    // Если предметы разные — меняем местами (Свап)
                    else if (targetContainer.canInsert(targetSlot, sourceStack) && (targetStack.isEmpty() || sourceContainer.canInsert(sourceSlot, targetStack))) {
                        sourceContainer.setStack(sourceSlot, targetStack);
                        targetContainer.setStack(targetSlot, sourceStack);

                        ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, sourceContainer.getStack(sourceSlot));
                        ContainerSlotUpdatePacket.send(player, targetId, targetSlot, targetContainer.getStack(targetSlot));
                        needsWeightSync = true;
                    }
                }
            } else if (action == ContainerActionType.EQUIP && sourceId == 0) {
                IContainer eqContainer = containers.getEquipment();
                for (int i = 0; i < eqContainer.getSize(); i++) {
                    if (eqContainer.getStack(i).isEmpty() && eqContainer.canInsert(i, sourceStack)) {
                        eqContainer.setStack(i, sourceStack);
                        sourceContainer.setStack(sourceSlot, ItemStack.EMPTY);

                        ContainerSlotUpdatePacket.send(player, 1, i, eqContainer.getStack(i));
                        ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, ItemStack.EMPTY);
                        needsWeightSync = true;
                        break;
                    }
                }
            } else if (action == ContainerActionType.UNEQUIP && sourceId == 1) {
                IContainer invContainer = containers.getInventory();
                for (int i = 0; i < maxInvSlots; i++) {
                    if (invContainer.getStack(i).isEmpty()) {
                        invContainer.setStack(i, sourceStack);
                        sourceContainer.setStack(sourceSlot, ItemStack.EMPTY);

                        ContainerSlotUpdatePacket.send(player, 0, i, invContainer.getStack(i));
                        ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, ItemStack.EMPTY);
                        needsWeightSync = true;
                        break;
                    }
                }
            } else if (action == ContainerActionType.DROP) {
                ItemEntity itemEntity = player.dropItem(sourceStack.copy(), false, true);
                if (itemEntity != null) {
                    sourceContainer.setStack(sourceSlot, ItemStack.EMPTY);
                    ContainerSlotUpdatePacket.send(player, sourceId, sourceSlot, ItemStack.EMPTY);
                    needsWeightSync = true;
                }
            }
        }

        if (needsWeightSync) {
            com.conquest.hud.core.stats.WeightManager.updateServerWeight(player);
            syncVanillaEquipment(player, containers.getEquipment());
        }
    }

    public static void syncVanillaEquipment(ServerPlayerEntity player, IContainer eq) {
        int activeSlot = activeWeapons.getOrDefault(player.getUuid(), -1);
        if (activeSlot >= 0 && activeSlot < eq.getSize()) {
            player.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, eq.getStack(activeSlot).copy());
        } else {
            player.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }

        player.equipStack(net.minecraft.entity.EquipmentSlot.HEAD, eq.getStack(EquipmentSlot.HEAD.getIndex()).copy());
        player.equipStack(net.minecraft.entity.EquipmentSlot.CHEST, eq.getStack(EquipmentSlot.TORSO.getIndex()).copy());
        player.equipStack(net.minecraft.entity.EquipmentSlot.LEGS, eq.getStack(EquipmentSlot.PANTS.getIndex()).copy());
        player.equipStack(net.minecraft.entity.EquipmentSlot.FEET, eq.getStack(EquipmentSlot.BOOTS.getIndex()).copy());
    }

    private static IContainer getContainer(IPlayerContainers containers, int id) {
        if (id == 0) return containers.getInventory();
        if (id == 1) return containers.getEquipment();
        if (id == 2) return containers.getHotbar();
        return null;
    }
}