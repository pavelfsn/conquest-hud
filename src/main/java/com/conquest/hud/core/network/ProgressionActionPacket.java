package com.conquest.hud.core.network;

import com.conquest.hud.core.progression.IProgressionComponent;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class ProgressionActionPacket {
    public static final Identifier ID = new Identifier("conquest", "progression_action");

    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            int actionType = buf.readInt(); // 0 = Стат, 1 = Оружие, 2 = Сброс
            int index = buf.readInt();

            server.execute(() -> {
                IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
                if (prog == null) return;

                if (actionType == 0) {
                    prog.upgradeStat(index);
                } else if (actionType == 1) {
                    prog.upgradeWeaponSkill(index);
                } else if (actionType == 2) {
                    prog.tryRespec();
                }

                // Пересчитываем вес и стамину, так как Сила или Ловкость могли измениться
                com.conquest.hud.core.stats.WeightManager.updateServerWeight(player);
            });
        });
    }
}