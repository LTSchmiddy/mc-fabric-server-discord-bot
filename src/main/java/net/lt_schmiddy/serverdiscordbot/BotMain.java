package net.lt_schmiddy.serverdiscordbot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.lt_schmiddy.serverdiscordbot.bot.ServerBot;
import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;
import net.minecraft.server.MinecraftServer;

public class BotMain implements ModInitializer, ServerStopping {

	public static ServerBot[] bots;
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Loading Discord Bots...");
		ConfigHandler.load();

		bots = new ServerBot[ConfigHandler.config.bots.length];
		for (int i = 0; i < bots.length; i++) {
			bots[i] = new ServerBot(ConfigHandler.config.bots[i]);
		}

		// Registering server shutdown event:
		ServerLifecycleEvents.SERVER_STOPPING.register(this);
	}

	@Override
	public void onServerStopping(MinecraftServer server) {
		System.out.println("Shutting down discord bot(s)");

		for (int i = 0; i < bots.length; i++) {
			if (bots[i].isDead()) {continue;}
			bots[i].onQuit();
		}
	}
}
