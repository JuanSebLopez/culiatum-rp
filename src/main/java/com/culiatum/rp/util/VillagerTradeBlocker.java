package com.culiatum.rp.util;

import com.culiatum.rp.ModConfig;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public final class VillagerTradeBlocker {
	private static final String EASY_NPC_PACKAGE_PREFIX = "de.markusbordihn.easynpc.";
	private static final String[] EASY_NPC_MARKERS = {
		"de.markusbordihn.easynpc.entity.easynpc.EasyNPCEntityAccess",
		"de.markusbordihn.easynpc.entity.easynpc.npc.StandardEasyNPC"
	};

	private VillagerTradeBlocker() {
	}

	public static void initialize() {
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			if (level.isClientSide()) {
				return InteractionResult.PASS;
			}

			if (!shouldBlock(entity)) {
				return InteractionResult.PASS;
			}

			player.displayClientMessage(Component.literal("Trading with this merchant is disabled on this server.").withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		});
	}

	private static boolean shouldBlock(Entity entity) {
		if (isEasyNpcMerchant(entity)) {
			return false;
		}

		if (entity instanceof Villager) {
			return ModConfig.isVillagerTradingDisabled();
		}

		if (entity instanceof WanderingTrader) {
			return ModConfig.isWanderingTraderTradingDisabled();
		}

		return false;
	}

	private static boolean isEasyNpcMerchant(Entity entity) {
		Class<?> currentClass = entity.getClass();
		while (currentClass != null) {
			if (isEasyNpcType(currentClass)) {
				return true;
			}

			for (Class<?> interfaceClass : currentClass.getInterfaces()) {
				if (isEasyNpcType(interfaceClass)) {
					return true;
				}
			}

			currentClass = currentClass.getSuperclass();
		}

		return false;
	}

	private static boolean isEasyNpcType(Class<?> type) {
		String name = type.getName();
		if (name.startsWith(EASY_NPC_PACKAGE_PREFIX)) {
			return true;
		}

		for (String marker : EASY_NPC_MARKERS) {
			if (marker.equals(name)) {
				return true;
			}
		}

		return false;
	}
}
