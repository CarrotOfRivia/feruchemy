package com.example.feruchemy.commands;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.powers.command.AllomancyPowerType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;

public class FeruchemyCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        LiteralArgumentBuilder<CommandSource> feruchemyCommand = Commands.literal("feruchemy")
                .requires(player -> player.hasPermissionLevel(2));

        feruchemyCommand
                .then(Commands.argument("player", EntityArgument.player()).then(Commands.literal("become").then(Commands.literal("Feruchemist").executes(
                context -> {FeruchemyCapability.addAll(EntityArgument.getPlayer(context, "player")); return 1;}))))

                .then(Commands.argument("player", EntityArgument.player()).then(Commands.literal("add").then(Commands.argument("type", AllomancyPowerType.INSTANCE).executes(
                        context -> {
                            String type = context.getArgument("type", String.class).toUpperCase();
                            if ("all".equalsIgnoreCase(type)) {
                                FeruchemyCapability.addAll(EntityArgument.getPlayer(context, "player"));
                            } else {
                                Metal mt = Metal.valueOf(type.toUpperCase());
                                FeruchemyCapability.addPower(mt, EntityArgument.getPlayer(context, "player"));
                            }
                            return 1;}
                ))))

                .then(Commands.argument("player", EntityArgument.player()).then(Commands.literal("remove").then(Commands.argument("type", AllomancyPowerType.INSTANCE)).executes(
                        context -> {
                            String type = context.getArgument("type", String.class).toUpperCase();
                            if ("all".equalsIgnoreCase(type)) {
                                FeruchemyCapability.revokeAll(EntityArgument.getPlayer(context, "player"));
                            } else {
                                Metal mt = Metal.valueOf(type.toUpperCase());
                                FeruchemyCapability.revokePower(mt, EntityArgument.getPlayer(context, "player"));
                            }
                            return 1;}
                )));

        dispatcher.register(feruchemyCommand);
    }
}
