package net.lt_schmiddy.serverdiscordbot.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class OnPlayerConnectMixin {
	@Inject(at = @At("RETURN"), method = "onPlayerConnect")
	private void player_connection(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		System.out.println("Discord: player connecting...");
		System.out.println(connection);
		System.out.println(player);
	}	
	
	@Inject(at = @At("RETURN"), method = "remove")
	private void remove(ServerPlayerEntity player, CallbackInfo info) {
		System.out.println("Discord: player disconnecting...");
		System.out.println(player);
	}
}
