package com.conquest.hud.client.network;

import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.IPlayerContainers;
import com.conquest.hud.core.network.ContainerSyncPacket;
import com.conquest.hud.core.network.ContainerSlotUpdatePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ModClientNetworking {

    public static void registerClientReceivers() {
        // Полная синхронизация (вызывается при подключении или массовых операциях)
        ClientPlayNetworking.registerGlobalReceiver(ContainerSyncPacket.ID, (client, handler, buf, responseSender) -> {
            int containerId = buf.readInt();
            int size = buf.readInt();
            DefaultedList<ItemStack> items = DefaultedList.ofSize(size, ItemStack.EMPTY);

            for (int i = 0; i < size; i++) {
                items.set(i, buf.readItemStack());
            }

            client.execute(() -> {
                if (client.player == null) return;
                IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);

                if (containerId == 0) updateClientContainer(containers.getInventory().getItems(), items);
                else if (containerId == 1) updateClientContainer(containers.getEquipment().getItems(), items);
                else if (containerId == 2) updateClientContainer(containers.getHotbar().getItems(), items);
            });

        });

        // Точечная синхронизация одного слота (оптимизация)
        ClientPlayNetworking.registerGlobalReceiver(ContainerSlotUpdatePacket.ID, (client, handler, buf, responseSender) -> {
            int containerId = buf.readInt();
            int slotIndex = buf.readInt();
            ItemStack stack = buf.readItemStack();

            client.execute(() -> {
                if (client.player == null) return;
                IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(client.player);

                if (containerId == 0 && slotIndex >= 0 && slotIndex < containers.getInventory().getSize()) {
                    containers.getInventory().setStack(slotIndex, stack);
                } else if (containerId == 1 && slotIndex >= 0 && slotIndex < containers.getEquipment().getSize()) {
                    containers.getEquipment().setStack(slotIndex, stack);
                } else if (containerId == 2 && slotIndex >= 0 && slotIndex < containers.getHotbar().getSize()) {
                    containers.getHotbar().setStack(slotIndex, stack);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(com.conquest.hud.core.network.LevelUpPacket.ID, (client, handler, buf, responseSender) -> {
            int oldLvl = buf.readInt();
            int newLvl = buf.readInt();
            client.execute(() -> com.conquest.hud.client.render.LevelUpHudRenderer.trigger(oldLvl, newLvl));
        });
    }

    private static void updateClientContainer(DefaultedList<ItemStack> target, DefaultedList<ItemStack> source) {
        for (int i = 0; i < target.size() && i < source.size(); i++) {
            target.set(i, source.get(i));
        }
    }
}