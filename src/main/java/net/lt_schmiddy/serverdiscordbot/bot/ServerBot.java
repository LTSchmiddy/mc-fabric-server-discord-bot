package net.lt_schmiddy.serverdiscordbot.bot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
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
            jda.awaitReady();
            startup();
        } catch (LoginException|InterruptedException e) {
            System.out.println(e);
            jda = null;
        }
    }
    void startup() {
        jda.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.playing(config.startup_message));

    }
    
    public boolean isDead() {
        return jda == null;
    }

    public void onServerStart() {
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing(BotMain.getServer().getServerMotd()));
    }

    public void onServerStop() {
        jda.getPresence().setActivity(Activity.playing(config.shutdown_message));
        jda.shutdown();
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
}
