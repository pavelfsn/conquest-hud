package com.conquest.hud.core.command;

import com.conquest.hud.core.logger.ModLogger;
import com.conquest.hud.core.stats.IPlayerStats;
import com.conquest.hud.core.stats.StatsComponentRegistry;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RpgCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("rpg")
                    .requires(source -> source.hasPermissionLevel(2))
                    .then(CommandManager.literal("set")
                            .then(CommandManager.argument("stat", StringArgumentType.word())
                                    .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                if (player == null) return 0;

                                                String stat = StringArgumentType.getString(context, "stat").toLowerCase();
                                                int value = IntegerArgumentType.getInteger(context, "value");
                                                IPlayerStats stats = StatsComponentRegistry.PLAYER_STATS.get(player);
                                                if (stats == null) {
                                                    context.getSource().sendError(Text.literal("Stats component not available"));
                                                    return 0;
                                                }

                                                switch (stat) {
                                                    case "strength" -> stats.setStrength(value);
                                                    case "agility" -> stats.setAgility(value);
                                                    case "vitality" -> stats.setVitality(value);
                                                    case "metabolism" -> stats.setMetabolism(value);
                                                    case "intellect" -> stats.setIntellect(value);
                                                    default -> {
                                                        context.getSource().sendError(Text.literal("Unknown stat. Use: strength, agility, vitality, metabolism, intellect"));
                                                        return 0;
                                                    }
                                                }
                                                ModLogger.info("STATS", "Admin changed " + stat + " to " + value + " for " + player.getName().getString());
                                                context.getSource().sendFeedback(() -> Text.literal("Stat " + stat + " set to " + value), false);
                                                return 1;
                                            })
                                    )
                            )
                    )
            );
        });
    }
}