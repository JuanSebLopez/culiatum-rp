package com.culiatum.rp.item;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Set;

public final class RecallPotionItem extends Item {
	public RecallPotionItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
		super.finishUsingItem(stack, level, livingEntity);

		if (!(livingEntity instanceof ServerPlayer player)) {
			return stack;
		}

		MinecraftServer server = level.getServer();
		ServerLevel overworld = server.overworld();
		BlockPos spawnPos = overworld.getLevelData().getRespawnData().pos();
		float spawnAngle = overworld.getLevelData().getRespawnData().yaw();

		player.teleportTo(overworld, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, Set.of(), spawnAngle, 0.0F, false);
		player.resetFallDistance();
		player.displayClientMessage(Component.literal("The Recall Potion returned you to spawn."), true);
		overworld.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);

		if (!player.getAbilities().instabuild) {
			stack.shrink(1);
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return stack;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 32;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack stack) {
		return ItemUseAnimation.DRINK;
	}

	@Override
	public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand usedHand) {
		player.startUsingItem(usedHand);
		return InteractionResult.CONSUME;
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.literal("Recall Potion");
	}
}
