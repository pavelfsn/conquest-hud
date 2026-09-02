package com.conquest.hud.client.network;

import com.conquest.hud.core.network.ContainerActionPacket;
import com.conquest.hud.core.network.ContainerActionType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public class ClientPacketSender {
    public static void sendAction(ContainerActionType action, int sourceContainerId, int sourceSlot, int targetContainerId, int targetSlot) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeEnumConstant(action);
        buf.writeInt(sourceContainerId);
        buf.writeInt(sourceSlot);
        buf.writeInt(targetContainerId);
        buf.writeInt(targetSlot);
        ClientPlayNetworking.send(ContainerActionPacket.ID, buf);
    }
}