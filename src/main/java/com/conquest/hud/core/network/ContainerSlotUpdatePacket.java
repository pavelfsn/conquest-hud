package com.conquest.hud.core.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ContainerSlotUpdatePacket {
    public static final Identifier ID = new Identifier("conquest", "container_slot_update");

    public static void send(ServerPlayerEntity player, int containerId, int slotIndex, ItemStack stack) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(containerId);
        buf.writeInt(slotIndex);
        buf.writeItemStack(stack);
        ServerPlayNetworking.send(player, ID, buf);
    }
}