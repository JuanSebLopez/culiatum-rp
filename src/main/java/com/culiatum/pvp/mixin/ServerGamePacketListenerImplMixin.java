package com.culiatum.pvp.mixin;

import com.culiatum.pvp.mixin.support.CommandBlocker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.chat.LastSeenMessages;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleChatCommand", at = @At("HEAD"), cancellable = true)
	private void culiatum$pvpBlockUnsigned(ServerboundChatCommandPacket packet, CallbackInfo ci) {
		if (CommandBlocker.shouldBlock(player, packet.command())) {
			player.displayClientMessage(Component.literal("You cannot use that command while in combat."), true);
			ci.cancel();
		}
	}

	@Inject(method = "performSignedChatCommand", at = @At("HEAD"), cancellable = true)
	private void culiatum$pvpBlockSigned(ServerboundChatCommandSignedPacket packet, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		if (CommandBlocker.shouldBlock(player, packet.command())) {
			player.displayClientMessage(Component.literal("You cannot use that command while in combat."), true);
			ci.cancel();
		}
	}
}
