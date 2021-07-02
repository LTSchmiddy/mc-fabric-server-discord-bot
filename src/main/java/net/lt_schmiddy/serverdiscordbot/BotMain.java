package net.lt_schmiddy.serverdiscordbot;

import com.mojang.authlib.GameProfile;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.*;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.lt_schmiddy.serverdiscordbot.bot.ServerBot;
import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;
import net.lt_schmiddy.serverdiscordbot.commands.*;
import net.minecraft.server.MinecraftServer;

import net.lt_schmiddy.serverdiscordbot.database.DiscordUserDatabase;

public class BotMain implements ModInitializer, ServerStarted, ServerStopping {

	static DiscordUserDatabase userDb;
	static DiscordPairCommand discordPairCommand;

	static MinecraftServer server;
	
	public static ServerBot[] bots;
	
	public static MinecraftServer getServer() {
		return server;
	}

	public static DiscordUserDatabase getUserDb() {
		return userDb;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loading Discord Bots...");
		ConfigHandler.load();

		// Setting up database:
		userDb = new DiscordUserDatabase();

		// Register Commands:
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			discordPairCommand = new DiscordPairCommand(dispatcher, dedicated);
        });

		// Registering server shutdown event:
		ServerLifecycleEvents.SERVER_STARTED.register(this);
		ServerLifecycleEvents.SERVER_STOPPING.register(this);

		bots = new ServerBot[ConfigHandler.config.bots.length];
		for (int i = 0; i < bots.length; i++) {
			bots[i] = new ServerBot(ConfigHandler.config.bots[i]);
		}
	}
	 
	@Override
	public void onServerStarted(MinecraftServer p_server) {
		server = p_server;

		for (ServerBot bot : bots) {
			bot.onServerStart();
		}	
	}
	@Override
	public void onServerStopping(MinecraftServer p_server) {
		System.out.println("Shutting down discord bot(s)");

		for (ServerBot bot : bots) {
			if (bot.isDead()) {continue;}
			bot.onServerStop();
		}

		ConfigHandler.save();
	}

	public static void onPlayerConnect() {}
	public static void onPlayerDisconnect() {}
	
	public static int onDiscordPairRequest(GameProfile profile, String discordId) {
		for (ServerBot bot : bots) {
			if (bot.isDead()) {continue;}
			if (bot.discordPairRequest(profile, discordId)) {
				return 0;
			}
		}

		return -1;
	}

	public static int onDiscordPairConfirm(GameProfile profile, String discordId, String authCode) {
		for (ServerBot bot : bots) {
			if (bot.isDead()) {continue;}
			if (bot.discordPairConfirm(profile, discordId, authCode)) {
				return 0;
			}
		}

		return -1;
	}
}
