package com.culiatum.rp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public final class ModConfig {
	private static final Path CONFIG_PATH = Path.of("config", "culiatum-rp.properties");

	private static int combatTagSeconds = 15;
	private static int radarCooldownSeconds = 5;
	private static int tpaCooldownSeconds = 120;
	private static int spawnCooldownSeconds = 120;
	private static int homeCooldownSeconds = 600;
	private static boolean opBypassValidations = true;
	private static boolean disableHomeSetCommands;
	private static boolean timeLimitSystemEnabled = true;
	private static boolean timeLimitEnforcementEnabled = true;
	private static String timeLimitTimezone = "America/Bogota";
	private static String timeLimitAction = "kick";
	private static String timeLimitKickMessage = "You reached today's play time limit. Come back after midnight in Bogota time.";
	private static int weekdayPaidSeconds = 21_600;
	private static int weekdayUnpaidSeconds = 7_200;
	private static int weekendPaidSeconds = 43_200;
	private static int weekendUnpaidSeconds = 21_600;
	private static boolean disableVillagerTrading = false;
	private static boolean disableWanderingTraderTrading = false;
	private static Set<String> blockedCommandPrefixes = new LinkedHashSet<>(Arrays.asList(
		"tpa",
		"tpaaccept",
		"tpaccept",
		"tpadeny",
		"tpdeny",
		"spawn"
	));

	private ModConfig() {
	}

	public static void load() {
		Properties properties = new Properties();

		try {
			Files.createDirectories(CONFIG_PATH.getParent());

			if (Files.exists(CONFIG_PATH)) {
				try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
					properties.load(inputStream);
				}
			}

			combatTagSeconds = readInt(properties, "combat_tag_seconds", 15, 5);
			radarCooldownSeconds = readInt(properties, "radar_cooldown_seconds", 5, 1);
			tpaCooldownSeconds = readInt(properties, "tpa_cooldown_seconds", 120, 0);
			spawnCooldownSeconds = readInt(properties, "spawn_cooldown_seconds", 120, 0);
			homeCooldownSeconds = readInt(properties, "home_cooldown_seconds", 600, 0);
			opBypassValidations = readBoolean(properties, "op_bypass_validations", true);
			disableHomeSetCommands = readBoolean(properties, "disable_home_set_commands", false);
			timeLimitSystemEnabled = readBoolean(properties, "system_enabled", true);
			timeLimitEnforcementEnabled = readBoolean(properties, "enforcement_enabled", true);
			timeLimitTimezone = readString(properties, "timezone", "America/Bogota");
			timeLimitAction = readString(properties, "limit_action", "kick");
			timeLimitKickMessage = readString(properties, "kick_message", "You reached today's play time limit. Come back after midnight in Bogota time.");
			weekdayPaidSeconds = readInt(properties, "weekday_paid_seconds", 21_600, 0);
			weekdayUnpaidSeconds = readInt(properties, "weekday_unpaid_seconds", 7_200, 0);
			weekendPaidSeconds = readInt(properties, "weekend_paid_seconds", 43_200, 0);
			weekendUnpaidSeconds = readInt(properties, "weekend_unpaid_seconds", 21_600, 0);
			disableVillagerTrading = readBoolean(properties, "disable_villager_trading", false);
			disableWanderingTraderTrading = readBoolean(properties, "disable_wandering_trader_trading", false);
			blockedCommandPrefixes = readCommandPrefixes(properties.getProperty(
				"blocked_command_prefixes",
				"tpa,tpaaccept,tpaccept,tpadeny,tpdeny,spawn"
			));

			save(properties);
		} catch (IOException exception) {
			CuliatumRpMod.LOGGER.error("Failed to load Culiatum RP configuration.", exception);
		}
	}

	private static boolean readBoolean(Properties properties, String key, boolean defaultValue) {
		String value = properties.getProperty(key);
		return value == null ? defaultValue : Boolean.parseBoolean(value);
	}

	private static int readInt(Properties properties, String key, int defaultValue, int minValue) {
		String value = properties.getProperty(key);

		if (value == null) {
			return defaultValue;
		}

		try {
			return Math.max(minValue, Integer.parseInt(value.trim()));
		} catch (NumberFormatException ignored) {
			return defaultValue;
		}
	}

	private static String readString(Properties properties, String key, String defaultValue) {
		String value = properties.getProperty(key);
		return value == null || value.isBlank() ? defaultValue : value.trim();
	}

	private static Set<String> readCommandPrefixes(String rawValue) {
		Set<String> values = new LinkedHashSet<>();

		for (String part : rawValue.split(",")) {
			String normalized = part.trim().toLowerCase();

			if (!normalized.isEmpty()) {
				values.add(normalized);
			}
		}

		return values;
	}

	public static int getCombatTagSeconds() {
		return combatTagSeconds;
	}

	public static int getRadarCooldownSeconds() {
		return radarCooldownSeconds;
	}

	public static int getTpaCooldownSeconds() {
		return tpaCooldownSeconds;
	}

	public static int getSpawnCooldownSeconds() {
		return spawnCooldownSeconds;
	}

	public static int getHomeCooldownSeconds() {
		return homeCooldownSeconds;
	}

	public static boolean isOpBypassValidationsEnabled() {
		return opBypassValidations;
	}

	public static boolean isHomeSetCommandsDisabled() {
		return disableHomeSetCommands;
	}

	public static boolean isTimeLimitSystemEnabled() {
		return timeLimitSystemEnabled;
	}

	public static boolean isTimeLimitEnforcementEnabled() {
		return timeLimitEnforcementEnabled;
	}

	public static String getTimeLimitTimezone() {
		return timeLimitTimezone;
	}

	public static String getTimeLimitAction() {
		return timeLimitAction;
	}

	public static String getTimeLimitKickMessage() {
		return timeLimitKickMessage;
	}

	public static int getWeekdayPaidSeconds() {
		return weekdayPaidSeconds;
	}

	public static int getWeekdayUnpaidSeconds() {
		return weekdayUnpaidSeconds;
	}

	public static int getWeekendPaidSeconds() {
		return weekendPaidSeconds;
	}

	public static int getWeekendUnpaidSeconds() {
		return weekendUnpaidSeconds;
	}

	public static boolean isVillagerTradingDisabled() {
		return disableVillagerTrading;
	}

	public static boolean isWanderingTraderTradingDisabled() {
		return disableWanderingTraderTrading;
	}

	public static Set<String> getBlockedCommandPrefixes() {
		return blockedCommandPrefixes;
	}

	public static void setTimeLimitSystemEnabled(boolean enabled) {
		timeLimitSystemEnabled = enabled;
		save();
	}

	public static void setTimeLimitEnforcementEnabled(boolean enabled) {
		timeLimitEnforcementEnabled = enabled;
		save();
	}

	public static void setOpBypassValidations(boolean enabled) {
		opBypassValidations = enabled;
		save();
	}

	private static void save() {
		save(new Properties());
	}

	private static void save(Properties properties) {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			properties.remove("disable_elytra");
			properties.setProperty("combat_tag_seconds", Integer.toString(combatTagSeconds));
			properties.setProperty("radar_cooldown_seconds", Integer.toString(radarCooldownSeconds));
			properties.setProperty("tpa_cooldown_seconds", Integer.toString(tpaCooldownSeconds));
			properties.setProperty("spawn_cooldown_seconds", Integer.toString(spawnCooldownSeconds));
			properties.setProperty("home_cooldown_seconds", Integer.toString(homeCooldownSeconds));
			properties.setProperty("op_bypass_validations", Boolean.toString(opBypassValidations));
			properties.setProperty("disable_home_set_commands", Boolean.toString(disableHomeSetCommands));
			properties.setProperty("system_enabled", Boolean.toString(timeLimitSystemEnabled));
			properties.setProperty("enforcement_enabled", Boolean.toString(timeLimitEnforcementEnabled));
			properties.setProperty("timezone", timeLimitTimezone);
			properties.setProperty("limit_action", timeLimitAction);
			properties.setProperty("kick_message", timeLimitKickMessage);
			properties.setProperty("weekday_paid_seconds", Integer.toString(weekdayPaidSeconds));
			properties.setProperty("weekday_unpaid_seconds", Integer.toString(weekdayUnpaidSeconds));
			properties.setProperty("weekend_paid_seconds", Integer.toString(weekendPaidSeconds));
			properties.setProperty("weekend_unpaid_seconds", Integer.toString(weekendUnpaidSeconds));
			properties.setProperty("disable_villager_trading", Boolean.toString(disableVillagerTrading));
			properties.setProperty("disable_wandering_trader_trading", Boolean.toString(disableWanderingTraderTrading));
			properties.setProperty("blocked_command_prefixes", String.join(",", blockedCommandPrefixes));

			try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
				properties.store(outputStream, "Culiatum RP config");
			}
		} catch (IOException exception) {
			CuliatumRpMod.LOGGER.error("Failed to save Culiatum RP configuration.", exception);
		}
	}
}
