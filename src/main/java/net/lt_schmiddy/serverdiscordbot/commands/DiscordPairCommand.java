package net.lt_schmiddy.serverdiscordbot.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.lt_schmiddy.serverdiscordbot.BotMain;
import net.minecraft.command.argument.*;

// import static net.minecraft.server.command.CommandManager.*;

public class DiscordPairCommand {
    public DiscordPairCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // Pair request:
        dispatcher.register(
            CommandManager.literal("discord_pair_request")
            .then(
                CommandManager.argument("discord_id", StringArgumentType.word())
                .executes(this::pair_request_self)
                .then(
                    CommandManager.argument("minecraft_name", GameProfileArgumentType.gameProfile())
                    .requires(source -> source.hasPermissionLevel(4))
                    .executes(this::pair_request_op)
                    .then(
                        CommandManager.argument("force", BoolArgumentType.bool())
                        .executes(this::pair_request_op_force)
                    )
                )
            )
        );

        // Pair confirm:
        dispatcher.register(CommandManager.literal("discord_pair_confirm")
            .then(
                CommandManager.argument("discord_id", StringArgumentType.word()).then(
                    CommandManager.argument("auth_code", StringArgumentType.word()).executes(this::pair_confirm_user)
                )
            )   
        );
        
        dispatcher.register(
            CommandManager.literal("discord_pair_uuid")
            .requires(source -> source.hasPermissionLevel(4))
            .then(
                CommandManager.argument("discord_id", StringArgumentType.word())
                .then(
                    CommandManager.argument("minecraft_uuid", StringArgumentType.word())
                    .executes(this::pair_request_op_uuid)
                )
            )
        );
        
        dispatcher.register(CommandManager.literal("discord_clear_all_requests")
            .requires(source -> source.hasPermissionLevel(4))
            .executes(this::clear_all_requests)
        );
    }

    private int pair_request_self(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // BotMain.onDiscordPair(context.getSource().getPlayer().getGameProfile(), context.getArgument("discord_name", String.class));
        return BotMain.onDiscordPairRequest(
            context.getSource().getPlayer().getGameProfile(),
            context.getArgument("discord_id", String.class)
        );
    }

    private int pair_request_op(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // try {
            return BotMain.onDiscordPairRequest(
                // context.getArgument("minecraft_name", GameProfile.class),
                (GameProfile)(GameProfileArgumentType.getProfileArgument(context, "minecraft_name").toArray()[0]),
                context.getArgument("discord_id", String.class)
            );
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return -1;
        // }
    }

    private int pair_request_op_uuid(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return BotMain.getUserDb().forcePair(
            context.getArgument("minecraft_uuid", String.class),
            context.getArgument("discord_id", String.class)
        );
    }

    private int pair_request_op_force(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        
        if (context.getArgument("force", Boolean.class)) {
            return BotMain.getUserDb().forcePair(
                (GameProfile)(GameProfileArgumentType.getProfileArgument(context, "minecraft_name").toArray()[0]),
                context.getArgument("discord_id", String.class)
            );
        } else {
            return pair_request_op(context);
        }
    }

    private int pair_confirm_user(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return BotMain.onDiscordPairConfirm(
            context.getSource().getPlayer().getGameProfile(),
            context.getArgument("discord_id", String.class),
            context.getArgument("auth_code", String.class)
        );
    }    
    
    private int clear_all_requests(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return BotMain.getUserDb().clearAllRequests();
        
    }

}
