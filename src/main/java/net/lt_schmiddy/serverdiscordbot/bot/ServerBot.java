package net.lt_schmiddy.serverdiscordbot.bot;

import javax.security.auth.login.LoginException;

import com.mojang.authlib.GameProfile;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.lt_schmiddy.serverdiscordbot.BotMain;
import net.lt_schmiddy.serverdiscordbot.config.BotConfig;

public class ServerBot extends ListenerAdapter {
    JDA jda;
    BotConfig config;

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

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
    //     if (event.isFromType(ChannelType.TEXT))
    //     {
    //         System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(),
    //                                 event.getMessage().getContentDisplay());
    //     }
    //     else
    //     {
    //         System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
    //                     event.getTextChannel().getName(), event.getMember().getEffectiveName(),
    //                     event.getMessage().getContentDisplay());
    //     }
    }
    
    // Methods:
    public boolean isDead() {
        return jda == null;
    }
    
    public void onServerStart() {
        try {
            jda.awaitReady();
            jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(BotMain.getServer().getServerMotd()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onServerStop() {
        jda.getPresence().setActivity(Activity.playing(config.shutdown_message));
        jda.shutdown();
    }

    public boolean discordPairRequest(GameProfile profile, String discordId) {
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
            channel.sendMessage("Auth Code: " + code).queue();
        });
        return true;
    }

    public boolean discordPairConfirm(GameProfile profile, String discordId, String authCode) {
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
    
}
