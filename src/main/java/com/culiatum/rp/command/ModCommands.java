package com.culiatum.rp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.culiatum.rp.item.ModItems;
import com.culiatum.rp.radar.RadarManager;
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
		dispatcher.register(
			Commands.literal("culiatumrp")
				.requires(ModCommands::isAdminSource)
				.then(Commands.literal("radar")
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
							.executes(ModCommands::showStatus))))
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

}
