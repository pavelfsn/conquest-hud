package com.conquest.hud.core.inventory;

import com.conquest.hud.ConquestPackets;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipTaskManager {
    private static final Map<UUID, EquipTask> activeTasks = new HashMap<>();

    public static class EquipTask {
        public final int slotId;
        public final int button;
        public final SlotActionType actionType;
        public final int maxTicks;
        public int currentTicks;
        public final Vec3d startPos;
        public final float startHealth;
        public final ItemStack snapshot; // Снимок предмета для защиты от дюпов

        public EquipTask(int slotId, int button, SlotActionType actionType, int maxTicks, Vec3d startPos, float startHealth, ItemStack snapshot) {
            this.slotId = slotId;
            this.button = button;
            this.actionType = actionType;
            this.maxTicks = maxTicks;
            this.currentTicks = 0;
            this.startPos = startPos;
            this.startHealth = startHealth;
            this.snapshot = snapshot.copy();
        }
    }

    public static void startTask(ServerPlayerEntity player, int slotId, int button, SlotActionType actionType, int delayTicks) {
        if (delayTicks <= 0) {
            executeTask(player, slotId, button, actionType);
            return;
        }

        // Получаем предмет в слоте на момент начала каста
        ItemStack snapshot = ItemStack.EMPTY;
        if (player.currentScreenHandler instanceof ConquestScreenHandler handler) {
            if (slotId >= 0 && slotId < handler.slots.size()) {
                snapshot = handler.slots.get(slotId).getStack();
            }
        }

        activeTasks.put(player.getUuid(), new EquipTask(slotId, button, actionType, delayTicks, player.getPos(), player.getHealth(), snapshot));
        ConquestPackets.sendActionSync(player, "Экипировка предмета...", delayTicks);
    }

    public static void tickTasks(Iterable<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            UUID uuid = player.getUuid();
            if (!activeTasks.containsKey(uuid)) continue;

            EquipTask task = activeTasks.get(uuid);

            // Отмена при получении урона, движении или закрытии инвентаря
            if (player.getHealth() < task.startHealth ||
                    player.getPos().squaredDistanceTo(task.startPos) > 0.01 ||
                    !(player.currentScreenHandler instanceof ConquestScreenHandler)) {

                cancelTask(uuid);
                ConquestPackets.sendActionSync(player, "", 0); // Отмена на клиенте
                continue;
            }

            task.currentTicks++;
            if (task.currentTicks >= task.maxTicks) {
                boolean isValid = false;
                if (player.currentScreenHandler instanceof ConquestScreenHandler handler) {
                    if (task.slotId >= 0 && task.slotId < handler.slots.size()) {
                        ItemStack currentStack = handler.slots.get(task.slotId).getStack();
                        // Строгая валидация снимка перед кликом
                        if (ItemStack.areEqual(currentStack, task.snapshot)) {
                            isValid = true;
                        }
                    }
                }

                if (isValid) {
                    executeTask(player, task.slotId, task.button, task.actionType);
                }
                cancelTask(uuid);
            }
        }
    }

    private static void executeTask(ServerPlayerEntity player, int slotId, int button, SlotActionType actionType) {
        if (player.currentScreenHandler instanceof ConquestScreenHandler handler) {
            handler.onSlotClick(slotId, button, actionType, player);
        }
    }

    public static boolean hasActiveTask(UUID playerId) {
        return activeTasks.containsKey(playerId);
    }

    public static void cancelTask(UUID playerId) {
        activeTasks.remove(playerId);
    }
}