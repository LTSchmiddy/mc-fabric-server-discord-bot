package net.lt_schmiddy.serverdiscordbot.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.lt_schmiddy.serverdiscordbot.BotMain;
import net.lt_schmiddy.serverdiscordbot.bot.ServerBot;
import net.minecraft.command.argument.*;
import net.minecraft.network.ClientConnection;

// import static net.minecraft.server.command.CommandManager.*;

public class DiscordPairCommand {
    public DiscordPairCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(CommandManager.literal("discord_pair")
                .then(CommandManager.argument("discord_id", StringArgumentType.word())
                    .executes(this::execute_user)
                        .then(CommandManager.argument("minecraft_name", GameProfileArgumentType.gameProfile())
                            .requires(source -> source.hasPermissionLevel(4)).executes(this::execute_op))));
    }

    public int execute_user(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // BotMain.onDiscordPair(context.getSource().getPlayer().getGameProfile(), context.getArgument("discord_name", String.class));
        return BotMain.onDiscordPair(context.getSource().getPlayer().getGameProfile(), context.getArgument("discord_id", String.class));
    }

    public int execute_op(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        BotMain.onDiscordPair(context.getArgument("minecraft_name", GameProfile.class), context.getArgument("discord_id", String.class));

        return 0;
    }

}
