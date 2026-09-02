package com.conquest.hud;

import com.conquest.hud.core.inventory.EquipTaskManager;
import com.conquest.hud.core.container.ContainerComponentRegistry;
import com.conquest.hud.core.container.IContainer;
import com.conquest.hud.core.container.IPlayerContainers;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

public class ConquestPackets {
    public static final Identifier EQUIP_CLICK_PACKET = new Identifier("conquest", "equip_click");
    public static final Identifier SYNC_ACTION_PACKET = new Identifier("conquest", "sync_action");
    public static final Identifier CHANGE_WEAPON_PACKET = new Identifier("conquest", "change_weapon");
    public static final Identifier OPEN_WEAPON_MOD_PACKET = new Identifier("conquest", "open_weapon_mod");

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
            SlotActionType actionType = SlotActionType.values()[buf.readInt()];

            server.execute(() -> {
                if (EquipTaskManager.hasActiveTask(player.getUuid())) return;
                int delay = (slotId >= 36 || actionType == SlotActionType.QUICK_MOVE) ? 60 : 0;
                EquipTaskManager.startTask(player, slotId, button, actionType, delay);
            });
        });

        // Открытие модификации оружия ИЗ ЛЮБОГО СЛОТА
        ServerPlayNetworking.registerGlobalReceiver(OPEN_WEAPON_MOD_PACKET, (server, player, handler, buf, responseSender) -> {
            int containerId = buf.readInt();
            int slotId = buf.readInt();
            server.execute(() -> {
                IPlayerContainers containers = ContainerComponentRegistry.CONTAINERS.get(player);
                IContainer container = null;
                if (containerId == 0) container = containers.getInventory();
                else if (containerId == 1) container = containers.getEquipment();
                else if (containerId == 2) container = containers.getHotbar();

                if (container != null && slotId >= 0 && slotId < container.getSize()) {
                    ItemStack weapon = container.getStack(slotId);
                    if (weapon.getItem() instanceof com.vicmatskiv.pointblank.attachment.AttachmentHost) {
                        // Кладём оружие в активный слот игрока, чтобы Point Blank корректно его отрисовал в редакторе
                        player.getInventory().setStack(player.getInventory().selectedSlot, weapon);

                        player.openHandledScreen(new net.minecraft.screen.SimpleNamedScreenHandlerFactory(
                                (syncId, inv, p) -> new com.vicmatskiv.pointblank.inventory.AttachmentContainerMenu(syncId, inv, weapon),
                                weapon.getName()
                        ));
                    }
                }
            });
        });
    }

    public static void sendActionSync(ServerPlayerEntity player, String text, int ticks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(text);
        buf.writeInt(ticks);
        ServerPlayNetworking.send(player, SYNC_ACTION_PACKET, buf);
    }
}