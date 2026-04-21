package com.culiatum.pvp.pvp;

import com.culiatum.pvp.ModConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatManager {
	private static final Map<UUID, Long> COMBAT_TAGS = new ConcurrentHashMap<>();

	private CombatManager() {
	}

	public static void handleDamage(LivingEntity entity, DamageSource source, float damageTaken) {
		if (damageTaken <= 0.0F || !(entity instanceof ServerPlayer victim)) {
			return;
		}

		ServerPlayer attacker = getResponsiblePlayer(source);

		if (attacker == null || attacker == victim) {
			return;
		}

		tag(victim);
		tag(attacker);
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

	private static void tag(ServerPlayer player) {
		long expiresAt = System.currentTimeMillis() + (ModConfig.getCombatTagSeconds() * 1000L);
		boolean wasTagged = isInCombat(player);
		COMBAT_TAGS.put(player.getUUID(), expiresAt);

		if (!wasTagged) {
			player.displayClientMessage(Component.literal("Entraste en combate. Teleports bloqueados temporalmente."), true);
		}
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
