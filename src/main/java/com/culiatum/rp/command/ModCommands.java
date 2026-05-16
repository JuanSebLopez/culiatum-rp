package com.culiatum.rp.command;

import com.culiatum.rp.pvp.CombatManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.culiatum.rp.item.HomeSetterItem;
import com.culiatum.rp.item.ModItems;
import com.culiatum.rp.radar.RadarManager;
import com.culiatum.rp.timelimit.TimeLimitCommands;
import com.culiatum.rp.vote.TimeVoteManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.item.ItemStack;

public final class ModCommands {
	private ModCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
								net.minecraft.commands.CommandBuildContext buildContext,
								Commands.CommandSelection environment) {
		var root = Commands.literal("culiatumrp")
			.requires(ModCommands::isAdminSource);

		root.then(Commands.literal("radar")
			.then(Commands.literal("give")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(ModCommands::giveRadar)))
			.then(Commands.literal("set")
				.then(Commands.argument("hunter", EntityArgument.player())
					.then(Commands.argument("target", EntityArgument.player())
						.then(Commands.argument("minutes", IntegerArgumentType.integer(1))
							.executes(ModCommands::setAssignment)
							.then(Commands.argument("label", StringArgumentType.greedyString())
								.executes(ModCommands::setAssignmentWithLabel))))))
			.then(Commands.literal("clear")
				.then(Commands.argument("hunter", EntityArgument.player())
					.executes(ModCommands::clearAssignment)))
			.then(Commands.literal("status")
				.then(Commands.argument("hunter", EntityArgument.player())
					.executes(ModCommands::showStatus))));

		root.then(Commands.literal("homesetter")
			.then(Commands.literal("give")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(ModCommands::giveHomeSetter)
					.then(Commands.argument("name", StringArgumentType.greedyString())
						.executes(ModCommands::giveNamedHomeSetter)))));

		root.then(Commands.literal("handcuffs")
			.then(Commands.literal("give")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(ModCommands::giveHandcuffs))));

		root.then(Commands.literal("pvp")
			.then(Commands.literal("enable")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(context -> setForcedPvp(context, true))))
			.then(Commands.literal("disable")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(context -> setForcedPvp(context, false))))
			.then(Commands.literal("enableall")
				.executes(context -> setGlobalForcedPvp(context, true)))
			.then(Commands.literal("disableall")
				.executes(context -> setGlobalForcedPvp(context, false)))
			.then(Commands.literal("status")
				.then(Commands.argument("player", EntityArgument.player())
					.executes(ModCommands::showPvpStatus))));

		root.then(TimeLimitCommands.create());
		dispatcher.register(root);

		dispatcher.register(
			Commands.literal("vote")
				.requires(source -> source.getEntity() instanceof ServerPlayer)
				.then(Commands.literal("day")
					.executes(context -> startVote(context, TimeVoteManager.VoteTarget.DAY))
					.then(Commands.literal("yes")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.DAY, true)))
					.then(Commands.literal("y")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.DAY, true)))
					.then(Commands.literal("no")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.DAY, false)))
					.then(Commands.literal("n")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.DAY, false))))
				.then(Commands.literal("night")
					.executes(context -> startVote(context, TimeVoteManager.VoteTarget.NIGHT))
					.then(Commands.literal("yes")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.NIGHT, true)))
					.then(Commands.literal("y")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.NIGHT, true)))
					.then(Commands.literal("no")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.NIGHT, false)))
					.then(Commands.literal("n")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.NIGHT, false))))
				.then(Commands.literal("clearweather")
					.executes(context -> startVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER))
					.then(Commands.literal("yes")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, true)))
					.then(Commands.literal("y")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, true)))
					.then(Commands.literal("no")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, false)))
					.then(Commands.literal("n")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, false))))
				.then(Commands.literal("clear")
					.executes(context -> startVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER))
					.then(Commands.literal("yes")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, true)))
					.then(Commands.literal("y")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, true)))
					.then(Commands.literal("no")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, false)))
					.then(Commands.literal("n")
						.executes(context -> castVote(context, TimeVoteManager.VoteTarget.CLEAR_WEATHER, false))))
		);
	}

	private static int giveRadar(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");

		if (RadarManager.hasRadar(player)) {
			context.getSource().sendFailure(Component.literal(player.getName().getString() + " already has a radar."));
			return 0;
		}

		ItemStack stack = new ItemStack(ModItems.RADAR);

		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}

		context.getSource().sendSuccess(() -> Component.literal("Radar given to " + player.getName().getString() + "."), true);
		return 1;
	}

	private static int giveHomeSetter(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ItemStack stack = HomeSetterItem.createStack(ModItems.HOME_SETTER, "Casa");

		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}

		context.getSource().sendSuccess(() -> Component.literal("Home Setter given to " + player.getName().getString() + "."), true);
		return 1;
	}

	private static int giveNamedHomeSetter(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		String homeName = StringArgumentType.getString(context, "name").trim();
		ItemStack stack = HomeSetterItem.createStack(ModItems.HOME_SETTER, homeName);

		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}

		context.getSource().sendSuccess(() -> Component.literal(
			"Home Setter for \"" + (homeName.isEmpty() ? "Casa" : homeName) + "\" given to " + player.getName().getString() + "."
		), true);
		return 1;
	}

	private static int giveHandcuffs(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		ItemStack stack = new ItemStack(ModItems.HANDCUFFS);

		if (!player.addItem(stack)) {
			player.drop(stack, false);
		}

		context.getSource().sendSuccess(() -> Component.literal("Handcuffs given to " + player.getName().getString() + "."), true);
		return 1;
	}

	private static int setAssignment(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		return setAssignment(context, null);
	}

	private static int setAssignmentWithLabel(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		return setAssignment(context, StringArgumentType.getString(context, "label"));
	}

	private static int setAssignment(CommandContext<CommandSourceStack> context, String label) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer hunter = EntityArgument.getPlayer(context, "hunter");
		ServerPlayer target = EntityArgument.getPlayer(context, "target");
		int minutes = IntegerArgumentType.getInteger(context, "minutes");

		if (!RadarManager.hasRadar(hunter)) {
			context.getSource().sendFailure(Component.literal(hunter.getName().getString() + " does not have a radar in their inventory."));
			return 0;
		}

		RadarManager.assignTarget(hunter, target, minutes, label);
		context.getSource().sendSuccess(() -> Component.literal(
			"Radar assigned to " + hunter.getName().getString() + " -> " + target.getName().getString() + " for " + minutes + " min."
		), true);
		return 1;
	}

	private static int clearAssignment(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer hunter = EntityArgument.getPlayer(context, "hunter");
		RadarManager.clearAssignment(hunter.getUUID());
		context.getSource().sendSuccess(() -> Component.literal("Target cleared for " + hunter.getName().getString() + "."), true);
		return 1;
	}

	private static int showStatus(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer hunter = EntityArgument.getPlayer(context, "hunter");
		Component status = RadarManager.buildStatusMessage(hunter);
		context.getSource().sendSuccess(() -> status, false);
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

	private static int startVote(CommandContext<CommandSourceStack> context, TimeVoteManager.VoteTarget target) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		return TimeVoteManager.startVote(player, target);
	}

	private static int castVote(CommandContext<CommandSourceStack> context, TimeVoteManager.VoteTarget target, boolean approve) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		return TimeVoteManager.castVote(player, target, approve);
	}

	private static int setForcedPvp(CommandContext<CommandSourceStack> context, boolean enabled) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		CombatManager.setForcedPvp(player, enabled);
		context.getSource().sendSuccess(() -> Component.literal(
			(enabled ? "Enabled" : "Disabled") + " forced PvP for " + player.getName().getString() + "."
		), true);
		return 1;
	}

	private static int setGlobalForcedPvp(CommandContext<CommandSourceStack> context, boolean enabled) {
		CombatManager.setGlobalForcedPvp(context.getSource().getServer(), enabled);
		context.getSource().sendSuccess(() -> Component.literal(
			enabled ? "Global PvP event mode enabled." : "Global PvP event mode disabled."
		), true);
		return 1;
	}

	private static int showPvpStatus(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(context, "player");
		boolean active = CombatManager.isForcedPvp(player);
		context.getSource().sendSuccess(() -> Component.literal(
			player.getName().getString() + " forced PvP status: " + (active ? "enabled" : "disabled") + "."
		), false);
		return 1;
	}
}
