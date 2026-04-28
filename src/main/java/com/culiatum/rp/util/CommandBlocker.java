package com.culiatum.rp.util;

import com.culiatum.rp.ModConfig;
import com.culiatum.rp.pvp.CombatManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class CommandBlocker {
	private static final Map<String, Long> COMMAND_COOLDOWNS = new ConcurrentHashMap<>();
	private static final Set<UUID> HOME_SET_BYPASS = ConcurrentHashMap.newKeySet();
	private static final Set<String> HOME_MANAGEMENT_SUBCOMMANDS = Set.of(
		"set",
		"delete",
		"remove",
		"del",
		"list",
		"rename",
		"clear",
		"public",
		"private"
	);

	private CommandBlocker() {
	}

	public static void initialize() {
	}

	public static Component handleCommand(ServerPlayer player, String rawCommand) {
		if (player == null || rawCommand == null) {
			return null;
		}

		if (canBypassValidations(player)) {
			return null;
		}

		String normalized = rawCommand.trim().toLowerCase();
		String[] parts = normalized.split("\\s+");
		String root = parts.length > 0 ? parts[0] : "";
		String second = parts.length > 1 ? parts[1] : "";

		if (ModConfig.isHomeSetCommandsDisabled() && isBlockedHomeSetCommand(root, second) && !HOME_SET_BYPASS.contains(player.getUUID())) {
			return Component.literal("Use a Home Setter item to set your home.").withStyle(ChatFormatting.RED);
		}

		TrackedCommand trackedCommand = getTrackedCommand(root, second);

		if (CombatManager.isInCombat(player) && (matchesBlockedCombatCommand(normalized) || trackedCommand == TrackedCommand.HOME)) {
			return Component.literal("You cannot use that command while in combat.").withStyle(ChatFormatting.RED);
		}

		if (trackedCommand == null) {
			return null;
		}

		int cooldownSeconds = trackedCommand.getCooldownSeconds();
		if (cooldownSeconds <= 0) {
			return null;
		}

		long now = System.currentTimeMillis();
		String cooldownKey = player.getUUID() + ":" + trackedCommand.cooldownKey();
		long expiresAt = COMMAND_COOLDOWNS.getOrDefault(cooldownKey, 0L);

		if (expiresAt > now) {
			long secondsLeft = Math.max(1L, (expiresAt - now + 999L) / 1000L);
			return Component.literal(trackedCommand.cooldownMessage(secondsLeft)).withStyle(ChatFormatting.YELLOW);
		}

		COMMAND_COOLDOWNS.put(cooldownKey, now + (cooldownSeconds * 1000L));
		return null;
	}

	public static <T> T runWithHomeSetBypass(ServerPlayer player, Supplier<T> action) {
		HOME_SET_BYPASS.add(player.getUUID());

		try {
			return action.get();
		} finally {
			HOME_SET_BYPASS.remove(player.getUUID());
		}
	}

	private static boolean canBypassValidations(ServerPlayer player) {
		if (!ModConfig.isOpBypassValidationsEnabled()) {
			return false;
		}

		return player.level().getServer() != null
			&& player.level().getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
	}

	private static boolean isBlockedHomeSetCommand(String root, String second) {
		return ("home".equals(root) && "set".equals(second)) || "sethome".equals(root);
	}

	private static boolean matchesBlockedCombatCommand(String normalized) {
		for (String prefix : ModConfig.getBlockedCommandPrefixes()) {
			if (normalized.equals(prefix) || normalized.startsWith(prefix + " ")) {
				return true;
			}
		}

		return false;
	}

	private static TrackedCommand getTrackedCommand(String root, String second) {
		if ("tpa".equals(root)) {
			return TrackedCommand.TPA;
		}

		if ("spawn".equals(root)) {
			return TrackedCommand.SPAWN;
		}

		if ("home".equals(root) && !HOME_MANAGEMENT_SUBCOMMANDS.contains(second)) {
			return TrackedCommand.HOME;
		}

		return null;
	}

	private enum TrackedCommand {
		TPA("tpa") {
			@Override
			int getCooldownSeconds() {
				return ModConfig.getTpaCooldownSeconds();
			}

			@Override
			String cooldownMessage(long secondsLeft) {
				return "You must wait " + secondsLeft + "s before using /tpa again.";
			}
		},
		SPAWN("spawn") {
			@Override
			int getCooldownSeconds() {
				return ModConfig.getSpawnCooldownSeconds();
			}

			@Override
			String cooldownMessage(long secondsLeft) {
				return "You must wait " + secondsLeft + "s before using /spawn again.";
			}
		},
		HOME("home") {
			@Override
			int getCooldownSeconds() {
				return ModConfig.getHomeCooldownSeconds();
			}

			@Override
			String cooldownMessage(long secondsLeft) {
				return "You must wait " + secondsLeft + "s before using /home again.";
			}
		};

		private final String cooldownKey;

		TrackedCommand(String cooldownKey) {
			this.cooldownKey = cooldownKey;
		}

		String cooldownKey() {
			return cooldownKey;
		}

		abstract int getCooldownSeconds();

		abstract String cooldownMessage(long secondsLeft);
	}
}
