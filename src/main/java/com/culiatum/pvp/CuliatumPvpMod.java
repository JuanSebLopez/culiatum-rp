package com.culiatum.pvp;

import com.culiatum.pvp.command.ModCommands;
import com.culiatum.pvp.item.ModItems;
import com.culiatum.pvp.mixin.support.CommandBlocker;
import com.culiatum.pvp.pvp.CombatManager;
import com.culiatum.pvp.radar.RadarManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CuliatumPvpMod implements ModInitializer {
	public static final String MOD_ID = "culiatum_pvp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModItems.register();
		registerEvents();
		CommandRegistrationCallback.EVENT.register(ModCommands::register);
		LOGGER.info("Culiatum PvP initialized. Combat, recall, and radar systems registered.");
	}

	private static void registerEvents() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
			CombatManager.handleDamage(entity, source, damageTaken));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) ->
			CombatManager.handleDeath(entity, source));
		EntityElytraEvents.ALLOW.register(entity -> !ModConfig.isElytraDisabled());
		CommandBlocker.initialize();
		RadarManager.initialize();
	}
}
