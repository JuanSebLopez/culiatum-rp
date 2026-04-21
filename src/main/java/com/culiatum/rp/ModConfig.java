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

	private static int combatTagSeconds = 30;
	private static int radarCooldownSeconds = 5;
	private static boolean disableElytra = true;
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

			combatTagSeconds = readInt(properties, "combat_tag_seconds", 30, 5);
			radarCooldownSeconds = readInt(properties, "radar_cooldown_seconds", 5, 1);
			disableElytra = readBoolean(properties, "disable_elytra", true);
			blockedCommandPrefixes = readCommandPrefixes(properties.getProperty(
				"blocked_command_prefixes",
				"tpa,tpaaccept,tpaccept,tpadeny,tpdeny,spawn"
			));

			properties.setProperty("combat_tag_seconds", Integer.toString(combatTagSeconds));
			properties.setProperty("radar_cooldown_seconds", Integer.toString(radarCooldownSeconds));
			properties.setProperty("disable_elytra", Boolean.toString(disableElytra));
			properties.setProperty("blocked_command_prefixes", String.join(",", blockedCommandPrefixes));

			try (OutputStream outputStream = Files.newOutputStream(CONFIG_PATH)) {
				properties.store(outputStream, "Culiatum PvP config");
			}
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

	public static boolean isElytraDisabled() {
		return disableElytra;
	}

	public static Set<String> getBlockedCommandPrefixes() {
		return blockedCommandPrefixes;
	}
}
