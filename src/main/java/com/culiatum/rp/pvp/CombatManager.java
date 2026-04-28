package com.culiatum.rp.pvp;

import com.culiatum.rp.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatManager {
	private static final Map<UUID, Long> COMBAT_TAGS = new ConcurrentHashMap<>();
	private static final Set<UUID> FORCED_PVP_PLAYERS = ConcurrentHashMap.newKeySet();
	private static volatile boolean globalForcedPvp;

	private CombatManager() {
	}

	public static void handleDamage(LivingEntity entity, DamageSource source, float damageTaken) {
		if (damageTaken <= 0.0F || !(entity instanceof ServerPlayer victim)) {
			return;
		}

		ServerPlayer attacker = getResponsiblePlayer(source);

		if (attacker != null && attacker != victim) {
			tag(victim);
			tag(attacker);
			return;
		}

		if (isHostileMobDamage(source)) {
			tag(victim);
		}
	}

	public static void handleDeath(LivingEntity entity, DamageSource source) {
		if (entity instanceof ServerPlayer player) {
			COMBAT_TAGS.remove(player.getUUID());
		}

		ServerPlayer attacker = getResponsiblePlayer(source);

		if (attacker != null) {
			tag(attacker);
		}
	}

	public static boolean isInCombat(ServerPlayer player) {
		if (isForcedPvp(player)) {
			return true;
		}

		Long expiresAt = COMBAT_TAGS.get(player.getUUID());

		if (expiresAt == null) {
			return false;
		}

		if (expiresAt <= System.currentTimeMillis()) {
			COMBAT_TAGS.remove(player.getUUID());
			return false;
		}

		return true;
	}

	public static void setForcedPvp(ServerPlayer player, boolean enabled) {
		boolean changed = enabled ? FORCED_PVP_PLAYERS.add(player.getUUID()) : FORCED_PVP_PLAYERS.remove(player.getUUID());

		if (changed) {
			player.sendSystemMessage(Component.literal(enabled
				? "Forced PvP mode was enabled for you. Teleport commands are blocked."
				: "Forced PvP mode was disabled for you."));
		}
	}

	public static void setGlobalForcedPvp(MinecraftServer server, boolean enabled) {
		globalForcedPvp = enabled;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			player.sendSystemMessage(Component.literal(enabled
				? "Global PvP event mode is now active. Teleport commands are blocked."
				: "Global PvP event mode has ended."));
		}
	}

	public static boolean isForcedPvp(ServerPlayer player) {
		return globalForcedPvp || FORCED_PVP_PLAYERS.contains(player.getUUID());
	}

	private static void tag(ServerPlayer player) {
		long expiresAt = System.currentTimeMillis() + (ModConfig.getCombatTagSeconds() * 1000L);
		boolean wasTagged = isInCombat(player);
		COMBAT_TAGS.put(player.getUUID(), expiresAt);

		if (!wasTagged) {
			player.displayClientMessage(Component.literal("You entered combat. Teleport commands are temporarily blocked."), true);
		}
	}

	private static boolean isHostileMobDamage(DamageSource source) {
		Entity attacker = source.getEntity();

		if (isHostileMob(attacker)) {
			return true;
		}

		Entity directEntity = source.getDirectEntity();

		if (directEntity instanceof Projectile projectile) {
			return isHostileMob(projectile.getOwner());
		}

		return isHostileMob(directEntity);
	}

	private static boolean isHostileMob(Entity entity) {
		if (!(entity instanceof LivingEntity livingEntity) || entity instanceof ServerPlayer) {
			return false;
		}

		return livingEntity instanceof Enemy || entity.getType() == EntityType.GHAST;
	}

	private static ServerPlayer getResponsiblePlayer(DamageSource source) {
		Entity attacker = source.getEntity();

		if (attacker instanceof ServerPlayer serverPlayer) {
			return serverPlayer;
		}

		Entity directEntity = source.getDirectEntity();

		if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer serverPlayer) {
			return serverPlayer;
		}

		return null;
	}
}
