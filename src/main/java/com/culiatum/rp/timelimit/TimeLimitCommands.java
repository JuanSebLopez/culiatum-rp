package com.culiatum.rp.timelimit;

import com.culiatum.rp.ModConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

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
					.then(Commands.argument("player", EntityArgument.player())
						.executes(context -> showPlayerStatus(context.getSource(), EntityArgument.getPlayer(context, "player")))))
				.then(Commands.literal("category")
					.then(Commands.argument("player", EntityArgument.player())
						.then(Commands.literal("paid")
							.executes(context -> setCategory(context.getSource(), EntityArgument.getPlayer(context, "player"), PlayerTimeCategory.PAID)))
						.then(Commands.literal("unpaid")
							.executes(context -> setCategory(context.getSource(), EntityArgument.getPlayer(context, "player"), PlayerTimeCategory.UNPAID)))))
				.then(Commands.literal("bypass")
					.then(Commands.argument("player", EntityArgument.player())
						.then(Commands.argument("enabled", BoolArgumentType.bool())
							.executes(context -> setBypass(
								context.getSource(),
								EntityArgument.getPlayer(context, "player"),
								BoolArgumentType.getBool(context, "enabled"))))))
				.then(Commands.literal("reset")
					.then(Commands.argument("player", EntityArgument.player())
						.executes(context -> resetPlayer(context.getSource(), EntityArgument.getPlayer(context, "player"))))))
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
			"Daily time limit system is " + (ModConfig.isTimeLimitSystemEnabled() ? "enabled" : "disabled") + "."
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

	private static int showPlayerStatus(CommandSourceStack source, ServerPlayer player) {
		source.sendSuccess(() -> TimeLimitManager.buildStatusMessage(source.getServer(), player), false);
		return 1;
	}

	private static int setCategory(CommandSourceStack source, ServerPlayer player, PlayerTimeCategory category) {
		TimeLimitManager.setPlayerCategory(source.getServer(), player, category);
		source.sendSuccess(() -> Component.literal(
			"Assigned " + category.name() + " category to " + player.getName().getString() + "."
		), true);
		return 1;
	}

	private static int setBypass(CommandSourceStack source, ServerPlayer player, boolean enabled) {
		TimeLimitManager.setPlayerBypass(source.getServer(), player, enabled);
		source.sendSuccess(() -> Component.literal(
			(enabled ? "Enabled" : "Disabled") + " time-limit bypass for " + player.getName().getString() + "."
		), true);
		return 1;
	}

	private static int resetPlayer(CommandSourceStack source, ServerPlayer player) {
		TimeLimitManager.resetPlayerUsage(source.getServer(), player.getUUID());
		source.sendSuccess(() -> Component.literal(
			"Reset today's time usage for " + player.getName().getString() + "."
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
}
