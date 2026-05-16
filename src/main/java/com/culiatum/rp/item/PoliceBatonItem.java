package com.culiatum.rp.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class PoliceBatonItem extends Item {
	private static final float DIRECT_DAMAGE = 3.0F;
	private static final int SLOWNESS_DURATION_TICKS = 80;
	private static final int NAUSEA_DURATION_TICKS = 60;

	public PoliceBatonItem(Properties properties) {
		super(properties);
	}

	public static Item.Properties createProperties(Item.Properties properties) {
		ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
			.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, -1.0D, AttributeModifier.Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
			)
			.build();
		return properties
			.stacksTo(1)
			.rarity(Rarity.UNCOMMON)
			.attributes(modifiers);
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);

		if (!(attacker.level() instanceof ServerLevel)) {
			return;
		}

		applyDirectDamage(target);
		applyControlEffects(target, attacker);
	}

	private static void applyDirectDamage(LivingEntity target) {
		float currentHealth = target.getHealth();
		if (currentHealth <= 1.0F) {
			return;
		}

		float newHealth = Math.max(1.0F, currentHealth - DIRECT_DAMAGE);
		target.setHealth(newHealth);
	}

	private static void applyControlEffects(LivingEntity target, LivingEntity attacker) {
		target.addEffect(createEffect(MobEffects.SLOWNESS, SLOWNESS_DURATION_TICKS, 0), attacker);
		target.addEffect(createEffect(MobEffects.NAUSEA, NAUSEA_DURATION_TICKS, 0), attacker);
	}

	private static MobEffectInstance createEffect(Holder<MobEffect> effect, int duration, int amplifier) {
		return new MobEffectInstance(effect, duration, amplifier, false, true, true);
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("item.culiatum_rp.police_baton");
	}
}
