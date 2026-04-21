package com.culiatum.rp.util;

import com.culiatum.rp.ModConfig;
import com.culiatum.rp.pvp.CombatManager;
import net.minecraft.server.level.ServerPlayer;

public final class CommandBlocker {
	private CommandBlocker() {
	}

	public static void initialize() {
	}

	public static boolean shouldBlock(ServerPlayer player, String rawCommand) {
		if (player == null || rawCommand == null || !CombatManager.isInCombat(player)) {
			return false;
		}

		String normalized = rawCommand.trim().toLowerCase();

		for (String prefix : ModConfig.getBlockedCommandPrefixes()) {
			if (normalized.equals(prefix) || normalized.startsWith(prefix + " ")) {
				return true;
			}
		}

		return false;
	}
}
