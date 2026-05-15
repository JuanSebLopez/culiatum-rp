package com.culiatum.rp.timelimit;

import com.culiatum.rp.ModConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public final class TimeLimitCommands {
	private TimeLimitCommands() {
	}

	public static LiteralArgumentBuilder<CommandSourceStack> create() {
		return Commands.literal("timelimit")
			.requires(TimeLimitCommands::isAdminSource)
			.then(Commands.literal("system")
				.then(Commands.literal("enable")
					.executes(context -> setSystemEnabled(context.getSource(), true)))
				.then(Commands.literal("disable")
					.executes(context -> setSystemEnabled(context.getSource(), false)))
				.then(Commands.literal("status")
					.executes(context -> showSystemStatus(context.getSource()))))
			.then(Commands.literal("enforcement")
				.then(Commands.literal("enable")
					.executes(context -> setEnforcementEnabled(context.getSource(), true)))
				.then(Commands.literal("disable")
					.executes(context -> setEnforcementEnabled(context.getSource(), false)))
				.then(Commands.literal("status")
					.executes(context -> showEnforcementStatus(context.getSource()))))
			.then(Commands.literal("opbypass")
				.then(Commands.literal("enable")
					.executes(context -> setOpBypass(context.getSource(), true)))
				.then(Commands.literal("disable")
					.executes(context -> setOpBypass(context.getSource(), false)))
				.then(Commands.literal("status")
					.executes(context -> showOpBypassStatus(context.getSource()))))
			.then(Commands.literal("player")
				.then(Commands.literal("status")
					.then(Commands.argument("player", StringArgumentType.word())
						.executes(context -> showPlayerStatus(context.getSource(), StringArgumentType.getString(context, "player")))))
				.then(Commands.literal("category")
					.then(Commands.argument("player", StringArgumentType.word())
						.then(Commands.literal("paid")
							.executes(context -> setCategory(
								context.getSource(),
								StringArgumentType.getString(context, "player"),
								PlayerTimeCategory.PAID)))
						.then(Commands.literal("unpaid")
							.executes(context -> setCategory(
								context.getSource(),
								StringArgumentType.getString(context, "player"),
								PlayerTimeCategory.UNPAID)))))
				.then(Commands.literal("bypass")
					.then(Commands.argument("player", StringArgumentType.word())
						.then(Commands.argument("enabled", BoolArgumentType.bool())
							.executes(context -> setBypass(
								context.getSource(),
								StringArgumentType.getString(context, "player"),
								BoolArgumentType.getBool(context, "enabled"))))))
				.then(Commands.literal("reset")
					.then(Commands.argument("player", StringArgumentType.word())
						.executes(context -> resetPlayer(context.getSource(), StringArgumentType.getString(context, "player"))))))
			.then(Commands.literal("resetall")
				.executes(context -> resetAll(context.getSource())))
			.then(Commands.literal("reload")
				.executes(context -> reload(context.getSource())));
	}

	private static int setSystemEnabled(CommandSourceStack source, boolean enabled) {
		ModConfig.setTimeLimitSystemEnabled(enabled);
		TimeLimitManager.reload(source.getServer());
		source.sendSuccess(() -> Component.literal(enabled
			? "Daily time limit system enabled."
			: "Daily time limit system disabled. Stored usage was preserved."), true);
		return 1;
	}

	private static int showSystemStatus(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(
			"Daily time limit system is "
				+ (ModConfig.isTimeLimitSystemEnabled() ? "enabled" : "disabled")
				+ " | Effective state: "
				+ (TimeLimitManager.isTimeLimitSystemActiveNow() ? "active" : "paused")
				+ " | Weekly pause: "
				+ (ModConfig.isTimeLimitWeeklyPauseEnabled()
					? ModConfig.getTimeLimitWeeklyPauseStartDay() + " " + ModConfig.getTimeLimitWeeklyPauseStartTime()
						+ " -> " + ModConfig.getTimeLimitWeeklyPauseEndDay() + " " + ModConfig.getTimeLimitWeeklyPauseEndTime()
					: "disabled")
		), false);
		return 1;
	}

	private static int setEnforcementEnabled(CommandSourceStack source, boolean enabled) {
		ModConfig.setTimeLimitEnforcementEnabled(enabled);
		TimeLimitManager.reload(source.getServer());
		source.sendSuccess(() -> Component.literal(enabled
			? "Daily time limit enforcement enabled."
			: "Daily time limit enforcement disabled."), true);
		return 1;
	}

	private static int showEnforcementStatus(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(
			"Daily time limit enforcement is " + (ModConfig.isTimeLimitEnforcementEnabled() ? "enabled" : "disabled") + "."
		), false);
		return 1;
	}

	private static int setOpBypass(CommandSourceStack source, boolean enabled) {
		ModConfig.setOpBypassValidations(enabled);
		source.sendSuccess(() -> Component.literal(enabled
			? "OP bypass is now enabled."
			: "OP bypass is now disabled."), true);
		return 1;
	}

	private static int showOpBypassStatus(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(
			"OP bypass is " + (ModConfig.isOpBypassValidationsEnabled() ? "enabled" : "disabled") + "."
		), false);
		return 1;
	}

	private static int showPlayerStatus(CommandSourceStack source, String playerName) {
		ResolvedPlayerTarget target = resolvePlayerTarget(source, playerName);
		if (target == null) {
			return 0;
		}
		source.sendSuccess(() -> TimeLimitManager.buildStatusMessage(source.getServer(), target.uuid(), target.label()), false);
		return 1;
	}

	private static int setCategory(CommandSourceStack source, String playerName, PlayerTimeCategory category) {
		ResolvedPlayerTarget target = resolvePlayerTarget(source, playerName);
		if (target == null) {
			return 0;
		}

		ServerPlayer onlinePlayer = source.getServer().getPlayerList().getPlayer(target.uuid());
		if (onlinePlayer != null) {
			TimeLimitManager.setPlayerCategory(source.getServer(), onlinePlayer, category);
		} else {
			TimeLimitManager.setPlayerCategory(source.getServer(), target.uuid(), category);
		}
		source.sendSuccess(() -> Component.literal(
			"Assigned " + category.name() + " category to " + target.label() + "."
		), true);
		return 1;
	}

	private static int setBypass(CommandSourceStack source, String playerName, boolean enabled) {
		ResolvedPlayerTarget target = resolvePlayerTarget(source, playerName);
		if (target == null) {
			return 0;
		}

		ServerPlayer onlinePlayer = source.getServer().getPlayerList().getPlayer(target.uuid());
		if (onlinePlayer != null) {
			TimeLimitManager.setPlayerBypass(source.getServer(), onlinePlayer, enabled);
		} else {
			TimeLimitManager.setPlayerBypass(source.getServer(), target.uuid(), enabled);
		}

		source.sendSuccess(() -> Component.literal(
			(enabled ? "Enabled" : "Disabled") + " time-limit bypass for " + target.label() + "."
		), true);
		return 1;
	}

	private static int resetPlayer(CommandSourceStack source, String playerName) {
		ResolvedPlayerTarget target = resolvePlayerTarget(source, playerName);
		if (target == null) {
			return 0;
		}
		TimeLimitManager.resetPlayerUsage(source.getServer(), target.uuid());
		source.sendSuccess(() -> Component.literal(
			"Reset today's time usage for " + target.label() + "."
		), true);
		return 1;
	}

	private static int resetAll(CommandSourceStack source) {
		TimeLimitManager.resetAllUsage(source.getServer());
		source.sendSuccess(() -> Component.literal("Reset today's time usage for all tracked players."), true);
		return 1;
	}

	private static int reload(CommandSourceStack source) {
		ModConfig.load();
		TimeLimitManager.reload(source.getServer());
		source.sendSuccess(() -> Component.literal("Reloaded Culiatum RP config and time-limit rules."), true);
		return 1;
	}

	private static boolean isAdminSource(CommandSourceStack source) {
		if (source.getEntity() == null) {
			return true;
		}

		if (source.getEntity() instanceof ServerPlayer player) {
			return source.getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
		}

		return false;
	}

	private static ResolvedPlayerTarget resolvePlayerTarget(CommandSourceStack source, String playerName) {
		ServerPlayer onlinePlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
		if (onlinePlayer != null) {
			return new ResolvedPlayerTarget(onlinePlayer.getUUID(), onlinePlayer.getName().getString());
		}

		ResolvedPlayerTarget cachedTarget = findInUserCache(playerName);
		if (cachedTarget != null) {
			return cachedTarget;
		}

		source.sendFailure(Component.literal("Unknown player: " + playerName + ". The player must have joined the server at least once."));
		return null;
	}

	private static ResolvedPlayerTarget findInUserCache(String playerName) {
		Path userCachePath = Path.of("usercache.json");
		if (!Files.exists(userCachePath)) {
			return null;
		}

		try {
			String rawJson = Files.readString(userCachePath);
			JsonElement root = JsonParser.parseString(rawJson);
			if (!(root instanceof JsonArray entries)) {
				return null;
			}

			for (JsonElement entryElement : entries) {
				if (!entryElement.isJsonObject()) {
					continue;
				}

				String cachedName = entryElement.getAsJsonObject().has("name")
					? entryElement.getAsJsonObject().get("name").getAsString()
					: "";
				String cachedUuid = entryElement.getAsJsonObject().has("uuid")
					? entryElement.getAsJsonObject().get("uuid").getAsString()
					: "";

				if (!cachedName.equalsIgnoreCase(playerName) || cachedUuid.isBlank()) {
					continue;
				}

				try {
					return new ResolvedPlayerTarget(UUID.fromString(cachedUuid), cachedName);
				} catch (IllegalArgumentException ignored) {
					return null;
				}
			}
		} catch (IOException | IllegalStateException ignored) {
			return null;
		}

		return null;
	}

	private record ResolvedPlayerTarget(UUID uuid, String label) {
	}
}
