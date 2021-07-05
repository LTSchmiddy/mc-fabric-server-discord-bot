package net.lt_schmiddy.serverdiscordbot.config;

public class BotConfig {
    public String bot_token = "ENTER_TOKEN_HERE";
    public String startup_message = "Server Startup...";
    public String online_message = "Online";
    public String shutdown_message = "Server Shutdown...";

    public BotLogConfig bot_log = new BotLogConfig();
    public BotRolesConfig bot_roles = new BotRolesConfig();
    public DiscordPairingConfig discordPairing = new DiscordPairingConfig();
}
