package com.culiatum.pvp.radar;

import com.culiatum.pvp.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RadarManager {
	private static final Map<UUID, RadarAssignment> ASSIGNMENTS = new ConcurrentHashMap<>();
	private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

	private RadarManager() {
	}

	public static void initialize() {
	}

	public static void assignTarget(ServerPlayer hunter, ServerPlayer target, int minutes, String label) {
		long expiresAt = System.currentTimeMillis() + (minutes * 60_000L);
		String missionLabel = label == null || label.isBlank() ? "Bounty Hunt" : label;
		ASSIGNMENTS.put(hunter.getUUID(), new RadarAssignment(target.getUUID(), target.getName().getString(), expiresAt, missionLabel));
		hunter.displayClientMessage(Component.literal("Assigned target: " + target.getName().getString() + "."), false);
	}

	public static void clearAssignment(UUID hunterUuid) {
		ASSIGNMENTS.remove(hunterUuid);
		COOLDOWNS.remove(hunterUuid);
	}

	public static void useRadar(ServerPlayer hunter) {
		RadarAssignment assignment = ASSIGNMENTS.get(hunter.getUUID());

		if (assignment == null) {
			hunter.displayClientMessage(Component.literal("This radar has no assigned target.").withStyle(ChatFormatting.RED), true);
			return;
		}

		if (assignment.expiresAt() <= System.currentTimeMillis()) {
			clearAssignment(hunter.getUUID());
			hunter.displayClientMessage(Component.literal("This radar assignment has expired.").withStyle(ChatFormatting.RED), true);
			return;
		}

		long now = System.currentTimeMillis();
		long cooldownEndsAt = COOLDOWNS.getOrDefault(hunter.getUUID(), 0L);

		if (cooldownEndsAt > now) {
			long secondsLeft = Math.max(1L, (cooldownEndsAt - now + 999L) / 1000L);
			hunter.displayClientMessage(Component.literal("Radar cooldown: " + secondsLeft + "s.").withStyle(ChatFormatting.YELLOW), true);
			return;
		}

		ServerPlayer target = hunter.level().getServer().getPlayerList().getPlayer(assignment.targetUuid());

		if (target == null) {
			hunter.displayClientMessage(Component.literal("Your target is offline.").withStyle(ChatFormatting.RED), true);
			return;
		}

		if (target.level() != hunter.level()) {
			String dimensionName = readableDimension(target.level());
			hunter.displayClientMessage(Component.literal("Your target is in another dimension: " + dimensionName + ".").withStyle(ChatFormatting.GOLD), true);
			COOLDOWNS.put(hunter.getUUID(), now + (ModConfig.getRadarCooldownSeconds() * 1000L));
			return;
		}

		int distance = (int) Math.floor(hunter.position().distanceTo(target.position()));
		long minutesLeft = Math.max(0L, (assignment.expiresAt() - now) / 60_000L);
		hunter.displayClientMessage(Component.literal(
			assignment.label() + ": " + target.getName().getString() + " is " + distance + " blocks away. Time remaining: " + minutesLeft + " min."
		).withStyle(ChatFormatting.AQUA), true);
		COOLDOWNS.put(hunter.getUUID(), now + (ModConfig.getRadarCooldownSeconds() * 1000L));
	}

	public static Component buildStatusMessage(ServerPlayer hunter) {
		RadarAssignment assignment = ASSIGNMENTS.get(hunter.getUUID());

		if (assignment == null) {
			return Component.literal("No target assigned.");
		}

		long remainingMillis = assignment.expiresAt() - System.currentTimeMillis();

		if (remainingMillis <= 0L) {
			clearAssignment(hunter.getUUID());
			return Component.literal("This radar assignment has expired.");
		}

		return Component.literal(
			"Target: " + assignment.targetName() + " | Mission: " + assignment.label() + " | Time remaining: " + Math.max(1L, remainingMillis / 60_000L) + " min."
		);
	}

	private static String readableDimension(Level level) {
		return level.dimension().identifier().getPath();
	}
}
