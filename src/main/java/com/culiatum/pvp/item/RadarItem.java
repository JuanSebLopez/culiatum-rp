package com.culiatum.pvp.item;

import com.culiatum.pvp.radar.RadarManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

public final class RadarItem extends Item {
	public RadarItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			RadarManager.useRadar(serverPlayer);
		}

		return InteractionResult.SUCCESS;
	}
}
