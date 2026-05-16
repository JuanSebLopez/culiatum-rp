package com.culiatum.rp.police;

import com.culiatum.rp.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class HandcuffsManager {
	public static final double RANGE = 1.0D;
	public static final int CUFF_DURATION_TICKS = 100;
	public static final int DETAIN_DURATION_TICKS = 1_200;
	private static final String DETAINED_TAG = "culiatum_detained";

	private static final Map<UUID, CuffAttempt> ACTIVE_ATTEMPTS = new HashMap<>();
	private static final Map<UUID, DetainedState> DETAINED_PLAYERS = new HashMap<>();

	private HandcuffsManager() {
	}

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(HandcuffsManager::tick);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			ACTIVE_ATTEMPTS.clear();
			DETAINED_PLAYERS.clear();
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			ACTIVE_ATTEMPTS.remove(handler.player.getUUID());
			releaseDetainedPlayer(handler.player);
		});
	}

	public static boolean isDetained(ServerPlayer player) {
		DetainedState state = DETAINED_PLAYERS.get(player.getUUID());
		return state != null && state.expiresAtTick() > player.level().getGameTime();
	}

	private static void tick(MinecraftServer server) {
		long gameTime = server.overworld().getGameTime();
		tickCuffAttempts(server, gameTime);
		tickDetainedPlayers(server, gameTime);
	}

	private static void tickCuffAttempts(MinecraftServer server, long gameTime) {
		Iterator<Map.Entry<UUID, CuffAttempt>> iterator = ACTIVE_ATTEMPTS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, CuffAttempt> entry = iterator.next();
			ServerPlayer officer = server.getPlayerList().getPlayer(entry.getKey());

			if (officer == null || !officer.isAlive() || !officer.isUsingItem() || !officer.getUseItem().is(ModItems.HANDCUFFS)) {
				iterator.remove();
				continue;
			}

			ServerPlayer currentTarget = resolveTargetPlayer(server, officer);
			if (currentTarget == null) {
				iterator.remove();
				continue;
			}

			CuffAttempt attempt = entry.getValue();
			if (!attempt.targetUuid().equals(currentTarget.getUUID())) {
				entry.setValue(new CuffAttempt(currentTarget.getUUID(), gameTime));
				continue;
			}

			if (isDetained(currentTarget)) {
				officer.stopUsingItem();
				officer.displayClientMessage(Component.literal(currentTarget.getName().getString() + " is already detained.").withStyle(ChatFormatting.YELLOW), true);
				iterator.remove();
				continue;
			}

			if (gameTime - attempt.startedAtTick() >= CUFF_DURATION_TICKS) {
				detainPlayer(currentTarget, gameTime);
				officer.stopUsingItem();
				officer.displayClientMessage(Component.literal(currentTarget.getName().getString() + " has been detained.").withStyle(ChatFormatting.GREEN), true);
				currentTarget.displayClientMessage(Component.literal("You have been detained for 1 minute.").withStyle(ChatFormatting.RED), true);
				iterator.remove();
			}
		}
	}

	private static void tickDetainedPlayers(MinecraftServer server, long gameTime) {
		Iterator<Map.Entry<UUID, DetainedState>> iterator = DETAINED_PLAYERS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<UUID, DetainedState> entry = iterator.next();
			ServerPlayer detainedPlayer = server.getPlayerList().getPlayer(entry.getKey());

			if (detainedPlayer == null) {
				iterator.remove();
				continue;
			}

			DetainedState state = entry.getValue();
			if (gameTime >= state.expiresAtTick()) {
				releaseDetainedPlayer(detainedPlayer);
				detainedPlayer.displayClientMessage(Component.literal("You are no longer detained.").withStyle(ChatFormatting.GREEN), true);
				iterator.remove();
				continue;
			}

			detainedPlayer.setDeltaMovement(0.0D, Math.min(0.0D, detainedPlayer.getDeltaMovement().y), 0.0D);
			detainedPlayer.hurtMarked = true;
			detainedPlayer.teleportTo((net.minecraft.server.level.ServerLevel) detainedPlayer.level(), state.x(), state.y(), state.z(), java.util.Set.of(), detainedPlayer.getYRot(), detainedPlayer.getXRot(), false);
		}
	}

	private static void detainPlayer(ServerPlayer player, long gameTime) {
		DETAINED_PLAYERS.put(player.getUUID(), new DetainedState(
			player.position().x,
			player.position().y,
			player.position().z,
			gameTime + DETAIN_DURATION_TICKS
		));
		player.addTag(DETAINED_TAG);
	}

	private static void releaseDetainedPlayer(ServerPlayer player) {
		DETAINED_PLAYERS.remove(player.getUUID());
		player.removeTag(DETAINED_TAG);
	}

	private static ServerPlayer resolveTargetPlayer(MinecraftServer server, ServerPlayer officer) {
		var target = HandcuffsTargeting.findTarget(officer, RANGE);
		if (target instanceof ServerPlayer serverPlayer && serverPlayer.level() == officer.level()) {
			return serverPlayer;
		}

		return null;
	}

	private record CuffAttempt(UUID targetUuid, long startedAtTick) {
	}

	private record DetainedState(double x, double y, double z, long expiresAtTick) {
	}
}
