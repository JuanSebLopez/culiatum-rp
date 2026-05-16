package com.culiatum.rp.police;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class HandcuffsTargeting {
	private HandcuffsTargeting() {
	}

	public static Player findTarget(Player officer, double range) {
		Vec3 eyePosition = officer.getEyePosition();
		Vec3 lookVector = officer.getViewVector(1.0F);
		Vec3 endPosition = eyePosition.add(lookVector.scale(range));

		EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
			officer.level(),
			officer,
			eyePosition,
			endPosition,
			officer.getBoundingBox().expandTowards(lookVector.scale(range)).inflate(1.0D),
			entity -> entity instanceof Player target
				&& entity.isAlive()
				&& entity != officer
				&& officer.distanceTo(target) <= range,
			(float) (range * range)
		);

		if (hitResult == null || !(hitResult.getEntity() instanceof Player target)) {
			return null;
		}

		return target;
	}
}
