package net.lt_schmiddy.serverdiscordbot.config;

public class BotConfig {
    public String bot_token = "ENTER_TOKEN_HERE";
    public String startup_message = "Server Startup...";
    public String shutdown_message = "Server Shutdown...";

    public DiscordPairingConfig discordPairing = new DiscordPairingConfig();
}
