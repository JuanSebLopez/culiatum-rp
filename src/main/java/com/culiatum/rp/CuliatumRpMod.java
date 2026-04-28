package com.culiatum.rp;

import com.culiatum.rp.command.ModCommands;
import com.culiatum.rp.item.ModItems;
import com.culiatum.rp.pvp.CombatManager;
import com.culiatum.rp.radar.RadarManager;
import com.culiatum.rp.timelimit.TimeLimitManager;
import com.culiatum.rp.util.CommandBlocker;
import com.culiatum.rp.util.VillagerTradeBlocker;
import com.culiatum.rp.vote.TimeVoteManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CuliatumRpMod implements ModInitializer {
	public static final String MOD_ID = "culiatum_rp";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModItems.register();
		registerEvents();
		CommandRegistrationCallback.EVENT.register(ModCommands::register);
		LOGGER.info("Culiatum RP initialized. Combat, recall, and radar systems registered.");
	}

	private static void registerEvents() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) ->
			CombatManager.handleDamage(entity, source, damageTaken));
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) ->
			CombatManager.handleDeath(entity, source));
		CommandBlocker.initialize();
		VillagerTradeBlocker.initialize();
		RadarManager.initialize();
		TimeLimitManager.initialize();
		ServerTickEvents.END_SERVER_TICK.register(TimeVoteManager::tick);
	}
}
