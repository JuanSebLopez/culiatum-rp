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
		if (entity instanceof Villager) {
			return ModConfig.isVillagerTradingDisabled();
		}

		if (entity instanceof WanderingTrader) {
			return ModConfig.isWanderingTraderTradingDisabled();
		}

		return false;
	}
}
