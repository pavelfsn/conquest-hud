package com.conquest.hud.core.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ContainerSyncPacket {
    public static final Identifier ID = new Identifier("conquest", "container_sync");

    public static void send(ServerPlayerEntity player, int containerId, DefaultedList<ItemStack> items) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(containerId);
        buf.writeInt(items.size());
        for (ItemStack stack : items) {
            buf.writeItemStack(stack);
        }
        ServerPlayNetworking.send(player, ID, buf);
    }
}