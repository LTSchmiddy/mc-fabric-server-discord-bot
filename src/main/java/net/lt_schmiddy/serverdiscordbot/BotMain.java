package net.lt_schmiddy.serverdiscordbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.lt_schmiddy.serverdiscordbot.bot.ServerBot;
import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;

public class BotMain implements ModInitializer, ServerStarted, ServerStopping {
	static MinecraftServer server;
	public static ServerBot[] bots;
	
	public static MinecraftServer getServer() {
		return server;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loading Discord Bots...");
		ConfigHandler.load();

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


}
