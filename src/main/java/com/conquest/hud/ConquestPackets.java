package com.conquest.hud;

import com.conquest.hud.core.inventory.EquipTaskManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ConquestPackets {
    public static final Identifier EQUIP_CLICK_PACKET = new Identifier("conquest", "equip_click");
    public static final Identifier SYNC_ACTION_PACKET = new Identifier("conquest", "sync_action");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(ConquestHud.OPEN_UI_PACKET, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                        (syncId, inventory, p) -> new com.conquest.hud.core.inventory.ConquestScreenHandler(syncId, inventory),
                        net.minecraft.text.Text.literal("Conquest UI")
                ));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(EQUIP_CLICK_PACKET, (server, player, handler, buf, responseSender) -> {
            int slotId = buf.readInt();
            int button = buf.readInt();
            // Восстанавливаем Enum из индекса
            SlotActionType actionType = SlotActionType.values()[buf.readInt()];

            server.execute(() -> {
                if (EquipTaskManager.hasActiveTask(player.getUuid())) return;

                int delay = (slotId >= 36 || actionType == SlotActionType.QUICK_MOVE) ? 60 : 0;
                EquipTaskManager.startTask(player, slotId, button, actionType, delay);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(CHANGE_WEAPON_PACKET, (server, player, handler, buf, responseSender) -> {
            int weaponType = buf.readInt(); // 1 - Primary, 2 - Secondary, 3 - Melee
            server.execute(() -> {
                // Временная заглушка для теста.
                // Здесь будет логика изъятия предмета из Trinkets и помещения в слот 0 ванильного инвентаря.
                System.out.println("[NET] " + player.getName().getString() + " requested weapon slot: " + weaponType);
            });
        });
    }

    public static final net.minecraft.util.Identifier CHANGE_WEAPON_PACKET = new net.minecraft.util.Identifier("conquest", "change_weapon");

    public static void sendActionSync(ServerPlayerEntity player, String text, int ticks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(text);
        buf.writeInt(ticks);
        ServerPlayNetworking.send(player, SYNC_ACTION_PACKET, buf);
    }
}