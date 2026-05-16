package com.culiatum.rp.item;

import com.culiatum.rp.police.HandcuffsManager;
import com.culiatum.rp.police.HandcuffsTargeting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public final class HandcuffsItem extends Item {
	public HandcuffsItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
		if (HandcuffsTargeting.findTarget(player, HandcuffsManager.RANGE) == null) {
			if (!level.isClientSide()) {
				player.displayClientMessage(Component.literal("You must aim at a player within 1 block.").withStyle(net.minecraft.ChatFormatting.RED), true);
			}
			return InteractionResult.FAIL;
		}

		player.startUsingItem(usedHand);
		return InteractionResult.CONSUME;
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
		if (!(interactionTarget instanceof Player target) || target == player || player.distanceTo(target) > HandcuffsManager.RANGE) {
			return InteractionResult.PASS;
		}

		player.startUsingItem(usedHand);
		return InteractionResult.CONSUME;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 72_000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.SPYGLASS;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("item.culiatum_rp.handcuffs");
	}
}
