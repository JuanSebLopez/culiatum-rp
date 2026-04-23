package com.culiatum.rp.item;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelData;

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

		teleportToRecallTarget(player);
		player.resetFallDistance();

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

	private static void teleportToRecallTarget(ServerPlayer player) {
		ServerLevel currentLevel = (ServerLevel) player.level();
		MinecraftServer server = currentLevel.getServer();
		ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();

		if (respawnConfig != null) {
			LevelData.RespawnData respawnData = respawnConfig.respawnData();
			TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);

			if (transition != null && respawnData != null) {
				ServerLevel respawnLevel = server.getLevel(respawnData.dimension());
				BlockPos respawnPos = respawnData.pos();
				player.teleport(transition);
				player.displayClientMessage(Component.literal("The Recall Potion returned you to your respawn point."), true);

				if (respawnLevel != null) {
					respawnLevel.playSound(null, respawnPos, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
				}

				return;
			}
		}

		ServerLevel overworld = server.overworld();
		BlockPos spawnPos = overworld.getLevelData().getRespawnData().pos();
		float spawnAngle = overworld.getLevelData().getRespawnData().yaw();

		player.teleportTo(overworld, spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, Set.of(), spawnAngle, 0.0F, false);
		player.displayClientMessage(Component.literal("The Recall Potion returned you to world spawn."), true);
		overworld.playSound(null, spawnPos, SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
	}
}
