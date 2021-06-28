package net.lt_schmiddy.serverdiscordbot;

import net.fabricmc.api.ModInitializer;


import net.lt_schmiddy.serverdiscordbot.bot.ServerBot;

import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;
public class BotMain implements ModInitializer {

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
	}
}
