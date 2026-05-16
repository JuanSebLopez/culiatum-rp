package com.culiatum.rp.item;

import com.mojang.brigadier.ParseResults;
import com.culiatum.rp.util.CommandBlocker;
import net.minecraft.core.component.DataComponents;
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
	private static final String DEFAULT_HOME_NAME = "Casa";

	public HomeSetterItem(Properties properties) {
		super(properties);
	}

	public static ItemStack createStack(Item item, String homeName) {
		ItemStack stack = new ItemStack(item);
		String normalizedHomeName = normalizeHomeName(homeName);

		if (!DEFAULT_HOME_NAME.equals(normalizedHomeName)) {
			stack.set(DataComponents.CUSTOM_NAME, Component.literal(normalizedHomeName + " Setter"));
		}

		return stack;
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
		ItemStack stack = player.getItemInHand(usedHand);
		String homeName = resolveTargetHomeName(stack);
		String commandToRun = resolveHomeSetCommand(server, source, homeName);

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

		serverPlayer.displayClientMessage(Component.literal("Your home \"" + homeName + "\" was set successfully.").withStyle(ChatFormatting.GREEN), true);
		return InteractionResult.SUCCESS_SERVER;
	}

	private static String resolveHomeSetCommand(MinecraftServer server, CommandSourceStack source, String homeName) {
		String quotedHomeName = quoteHomeName(homeName);

		if (canParse(server, source, "home set " + quotedHomeName)) {
			return "home set " + quotedHomeName;
		}

		if (canParse(server, source, "sethome " + quotedHomeName)) {
			return "sethome " + quotedHomeName;
		}

		return null;
	}

	private static String resolveTargetHomeName(ItemStack stack) {
		if (stack.has(DataComponents.CUSTOM_NAME)) {
			String customName = stack.getHoverName().getString().trim();
			if (customName.endsWith(" Setter")) {
				String stripped = customName.substring(0, customName.length() - " Setter".length()).trim();
				if (!stripped.isEmpty()) {
					return stripped;
				}
			}

			if (!customName.isEmpty()) {
				return customName;
			}
		}

		return DEFAULT_HOME_NAME;
	}

	private static String normalizeHomeName(String homeName) {
		if (homeName == null) {
			return DEFAULT_HOME_NAME;
		}

		String normalized = homeName.trim();
		return normalized.isEmpty() ? DEFAULT_HOME_NAME : normalized;
	}

	private static String quoteHomeName(String homeName) {
		return "\"" + homeName.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}

	private static boolean canParse(MinecraftServer server, CommandSourceStack source, String command) {
		ParseResults<CommandSourceStack> parseResults = server.getCommands().getDispatcher().parse(command, source);
		return !parseResults.getContext().getNodes().isEmpty() && !parseResults.getReader().canRead();
	}
}
