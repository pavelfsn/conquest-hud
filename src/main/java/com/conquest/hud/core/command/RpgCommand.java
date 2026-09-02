package com.conquest.hud.core.command;

import com.conquest.hud.core.logger.ModLogger;
import com.conquest.hud.core.progression.IProgressionComponent;
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
                    .then(CommandManager.literal("add")
                            .then(CommandManager.literal("xp")
                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                if (player == null) return 0;
                                                int amount = IntegerArgumentType.getInteger(context, "amount");
                                                IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(player);
                                                if (prog != null) {
                                                    prog.addXp(amount);
                                                    context.getSource().sendFeedback(() -> Text.literal("Выдано " + amount + " XP"), false);
                                                }
                                                return 1;
                                            })
                                    )
                            )
                    )
                    .then(CommandManager.literal("set")
                            .then(CommandManager.argument("stat", StringArgumentType.word())
                                    .then(CommandManager.argument("value", IntegerArgumentType.integer())
                                            .executes(context -> {
                                                ServerPlayerEntity player = context.getSource().getPlayer();
                                                if (player == null) return 0;

                                                String stat = StringArgumentType.getString(context, "stat").toLowerCase();
                                                int value = IntegerArgumentType.getInteger(context, "value");

                                                IProgressionComponent progression = StatsComponentRegistry.PROGRESSION.getNullable(player);
                                                if (progression == null) {
                                                    context.getSource().sendError(Text.literal("Progression component not available"));
                                                    return 0;
                                                }

                                                switch (stat) {
                                                    case "strength" -> progression.setStatRaw(0, value);
                                                    case "agility" -> progression.setStatRaw(1, value);
                                                    case "metabolism" -> progression.setStatRaw(2, value);
                                                    case "luck" -> progression.setStatRaw(3, value);
                                                    case "perception" -> progression.setStatRaw(4, value);
                                                    case "draw" -> progression.setWeaponSkillRaw(0, value);
                                                    case "reload" -> progression.setWeaponSkillRaw(1, value);
                                                    case "recoil" -> progression.setWeaponSkillRaw(2, value);
                                                    default -> {
                                                        context.getSource().sendError(Text.literal("Unknown stat."));
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
                    .then(CommandManager.literal("reset")
                            .then(CommandManager.argument("target", net.minecraft.command.argument.EntityArgumentType.player())
                                    .executes(context -> {
                                        ServerPlayerEntity target = net.minecraft.command.argument.EntityArgumentType.getPlayer(context, "target");
                                        IProgressionComponent prog = StatsComponentRegistry.PROGRESSION.getNullable(target);
                                        if (prog != null) {
                                            prog.forceRespec();
                                            context.getSource().sendFeedback(() -> Text.literal("Характеристики игрока " + target.getName().getString() + " сброшены!"), false);
                                        }
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}