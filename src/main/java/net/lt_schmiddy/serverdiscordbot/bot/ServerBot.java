package net.lt_schmiddy.serverdiscordbot.bot;

import javax.security.auth.login.LoginException;

import com.mojang.authlib.GameProfile;

import java.util.*;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.lt_schmiddy.serverdiscordbot.BotMain;
import net.lt_schmiddy.serverdiscordbot.config.BotConfig;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class ServerBot extends ListenerAdapter {
    JDA jda;
    BotConfig config;

    List<TextChannel> botLogChannels = new ArrayList<TextChannel>();
    List<Role> inGameRoles = new ArrayList<Role>();

    public ServerBot(BotConfig p_config) {
        config = p_config;

        try {
            jda = JDABuilder.createDefault(config.bot_token).addEventListeners(this).build();
        } catch (LoginException e) {
            System.out.println(e);
            jda = null;
        }
    }
    // EVENTS:
    @Override
    public void onReady(ReadyEvent event) {
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing(config.startup_message));

        // Assemble Log channels:
        for(String i : config.bot_log.channels) {
            TextChannel channel = jda.getTextChannelById(i);
            if (channel != null) {
                botLogChannels.add(channel);
                System.out.println("Channel '" + channel.getName() + "' in Guild '" + channel.getGuild().getName() + "' found.");
                channel.sendMessage("Server is starting...").queue();
            } else {
                System.out.println("Channel ID '" + i + "'' not found");
            }
        }

        // Assemble in-game roles:
        for (String i : config.bot_roles.in_game_roles) {
            Role role = jda.getRoleById(i);

            if (role != null) { 
                System.out.println("Role '" + role.getName() + "' in Guild '" + role.getGuild().getName() + "' found.");
            } else {
                System.out.println("Role ID '" + i + "' not found.");
            }
            inGameRoles.add(role);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {}
    
    // Methods:
    public boolean isDead() {
        return jda == null;
    }
    
    public void onServerStart() {
        try {
            jda.awaitReady();
            // jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(BotMain.getServer().getServerMotd()));
            for (Guild i : jda.getGuilds()) {
                i.modifyNickname(i.getSelfMember(), BotMain.getServer().getServerMotd()).queue();
            }

            for (TextChannel i : botLogChannels) {
                i.sendMessage("Server is online!").complete();
            }

            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(config.online_message));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onServerStop() {
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing(config.shutdown_message));
        for (TextChannel i : botLogChannels) {
            i.sendMessage("Server is shutting down...").complete();
        }

        jda.shutdown();
    }

    public boolean onDiscordPairRequest(GameProfile profile, String discordId) {
        if (!config.discordPairing.enabled) {return false;}
        
        User user = jda.retrieveUserById(discordId).complete();
        if (user == null) {
            System.out.println("User " + discordId + " not found");
            return false;
        }

        System.out.println(user.getName());

        String code = BotMain.getUserDb().createPairRequest(profile, discordId, config.discordPairing.pair_code_length);
        user.openPrivateChannel().queue((channel) ->
        {
            channel.sendMessage(
                "Auth Code: " + code
                + "\nTo confirm pairing, paste and run the following command in the Minecraft server:"
                + "\n`/discord_pair_confirm " + discordId + " " + code + "`"
            ).queue();
        });
        return true;
    }

    public boolean onDiscordPairConfirm(GameProfile profile, String discordId, String authCode) {
        if (!config.discordPairing.enabled) {return false;}
        User user = jda.retrieveUserById(discordId).complete();
        if (user == null) {
            System.out.println("User " + discordId + " not found");
            return false;
        }

        boolean result = BotMain.getUserDb().confirmPairRequest(profile, discordId, authCode);
        if (result) {
            user.openPrivateChannel().queue((channel) ->
            {
                channel.sendMessage("Account pairing confirmed!").queue();
            });
        }

        return result;
    }

    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        // Handle Bot Logging:
        if (config.bot_log.logPlayerConnect) {
            for (TextChannel i : botLogChannels){
                i.sendMessage(player.getGameProfile().getName() + " has joined " + BotMain.getServer().getServerMotd()).queue();
            }
        }

        // Handle Bot Roles:
        if (config.bot_roles.apply_roles){
            User user = BotMain.getUserDb().getDiscordUserFromMinecraft(jda, player.getGameProfile());
            System.out.println("user: " + user);
            if (user == null) {return;}

            for (Guild guild : jda.getGuilds()) {
                System.out.println("guild: " + guild);
                Member member = guild.retrieveMember(user).complete();
                System.out.println("member: " + member);
                if (member == null) {continue;}

                for (String rid : config.bot_roles.in_game_roles) {
                    Role role = guild.getRoleById(rid);
                    System.out.println("role: " + role);
                    if (role == null) {continue;}

                    guild.addRoleToMember(member, role).queue();
                }
            }
        }
    }
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        if (config.bot_log.logPlayerDisconnect) {
            for (TextChannel i : botLogChannels){
                i.sendMessage(player.getGameProfile().getName() + " has left " + BotMain.getServer().getServerMotd()).queue();;
            }
        }

        if (config.bot_roles.apply_roles){
            User user = BotMain.getUserDb().getDiscordUserFromMinecraft(jda, player.getGameProfile());
            System.out.println("user: " + user);
            if (user == null) {
                return;
            }

            for (Guild guild : jda.getGuilds()) {
                System.out.println("guild: " + guild);
                
                Member member = guild.retrieveMember(user).complete();
                System.out.println("member: " + member);
                if (member == null) {continue;}

                for (String rid : config.bot_roles.in_game_roles) {
                    Role role = guild.getRoleById(rid);
                    System.out.println("role: " + role);
                    if (role == null) {continue;}

                    guild.removeRoleFromMember(member, role).queue();
                }
            }
        }
    }

    // Utility Functions:

    
}
