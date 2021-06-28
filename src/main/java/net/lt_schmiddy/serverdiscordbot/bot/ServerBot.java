package net.lt_schmiddy.serverdiscordbot.bot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT))
        {
            System.out.printf("[PM] %s: %s\n", event.getAuthor().getName(),
                                    event.getMessage().getContentDisplay());
        }
        else
        {
            System.out.printf("[%s][%s] %s: %s\n", event.getGuild().getName(),
                        event.getTextChannel().getName(), event.getMember().getEffectiveName(),
                        event.getMessage().getContentDisplay());
        }
    }
}
