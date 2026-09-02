package com.conquest.hud.core.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class LevelUpPacket {
    public static final Identifier ID = new Identifier("conquest", "level_up");

    public static void send(ServerPlayerEntity player, int oldLevel, int newLevel) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(oldLevel);
        buf.writeInt(newLevel);
        ServerPlayNetworking.send(player, ID, buf);
    }
}