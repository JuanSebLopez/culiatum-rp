package com.culiatum.rp.item;

import com.mojang.brigadier.ParseResults;
import com.culiatum.rp.util.CommandBlocker;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class HomeSetterItem extends Item {
	private static final String HOME_NAME = "Casa";

	public HomeSetterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		MinecraftServer server = level.getServer();
		if (server == null) {
			return InteractionResult.FAIL;
		}

		CommandSourceStack source = serverPlayer.createCommandSourceStack().withSuppressedOutput();
		String commandToRun = resolveHomeSetCommand(server, source);

		if (commandToRun == null) {
			serverPlayer.displayClientMessage(Component.literal("Failed to set your home.").withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		CommandBlocker.runWithHomeSetBypass(serverPlayer, () -> {
			server.getCommands().performPrefixedCommand(source, commandToRun);
			return Boolean.TRUE;
		});

		if (!serverPlayer.getAbilities().instabuild) {
			player.getItemInHand(usedHand).shrink(1);
		}

		serverPlayer.displayClientMessage(Component.literal("Your home was set successfully.").withStyle(ChatFormatting.GREEN), true);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static String resolveHomeSetCommand(MinecraftServer server, CommandSourceStack source) {
		if (canParse(server, source, "home set " + HOME_NAME)) {
			return "home set " + HOME_NAME;
		}

		if (canParse(server, source, "sethome " + HOME_NAME)) {
			return "sethome " + HOME_NAME;
		}

		return null;
	}

	private static boolean canParse(MinecraftServer server, CommandSourceStack source, String command) {
		ParseResults<CommandSourceStack> parseResults = server.getCommands().getDispatcher().parse(command, source);
		return !parseResults.getContext().getNodes().isEmpty() && !parseResults.getReader().canRead();
	}
}
