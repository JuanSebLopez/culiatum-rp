package com.culiatum.rp.timelimit;

import com.culiatum.rp.ModConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TimeLimitManager {
	private static final Map<UUID, Long> SESSION_LAST_UPDATE = new ConcurrentHashMap<>();
	private static TimeLimitStorage storage;
	private static ZoneId zoneId = ZoneId.of("America/Bogota");

	private TimeLimitManager() {
	}

	public static void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(TimeLimitManager::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPING.register(TimeLimitManager::onServerStopping);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(handler.player, server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerDisconnect(handler.player, server));
		ServerTickEvents.END_SERVER_TICK.register(TimeLimitManager::tick);
	}

	public static void reload(MinecraftServer server) {
		zoneId = resolveZoneId();
		ensureStorage(server);
		ensureCurrentDate(server, false);
	}

	public static void setPlayerCategory(MinecraftServer server, ServerPlayer player, PlayerTimeCategory category) {
		setPlayerCategory(server, player.getUUID(), category);
		enforceIfNeeded(server, player);
	}

	public static void setPlayerCategory(MinecraftServer server, UUID playerUuid, PlayerTimeCategory category) {
		PlayerTimeData data = getData(server, playerUuid);
		data.setCategory(category);
		storage.setDirty();
	}

	public static void setPlayerBypass(MinecraftServer server, ServerPlayer player, boolean bypass) {
		setPlayerBypass(server, player.getUUID(), bypass);
		SESSION_LAST_UPDATE.put(player.getUUID(), System.currentTimeMillis());
	}

	public static void setPlayerBypass(MinecraftServer server, UUID playerUuid, boolean bypass) {
		PlayerTimeData data = getData(server, playerUuid);
		data.setBypass(bypass);
		storage.setDirty();
	}

	public static void resetPlayerUsage(MinecraftServer server, UUID playerUuid) {
		PlayerTimeData data = getData(server, playerUuid);
		data.resetUsage();
		storage.setDirty();
	}

	public static void resetAllUsage(MinecraftServer server) {
		ensureStorage(server);
		storage.removeAllUsage();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			SESSION_LAST_UPDATE.put(player.getUUID(), System.currentTimeMillis());
			player.sendSystemMessage(Component.literal("Your daily play time was reset by an administrator.").withStyle(ChatFormatting.GREEN));
		}
	}

	public static TimeLimitSnapshot getSnapshot(MinecraftServer server, UUID playerUuid) {
		ensureStorage(server);
		LocalDate today = getCurrentDate();
		PlayerTimeData data = getData(server, playerUuid);
		long limit = TimeLimitPolicy.getDailyLimitMillis(data.getCategory(), today);
		long remaining = Math.max(0L, limit - data.getUsedMillis());
		return new TimeLimitSnapshot(
			data.getCategory(),
			data.getUsedMillis(),
			limit,
			remaining,
			data.hasBypass(),
			ModConfig.isTimeLimitSystemEnabled(),
			ModConfig.isTimeLimitEnforcementEnabled()
		);
	}

	public static boolean isBypassed(MinecraftServer server, ServerPlayer player) {
		PlayerTimeData data = getData(server, player.getUUID());
		return data.hasBypass() || hasOpBypass(server, player);
	}

	public static Component buildStatusMessage(MinecraftServer server, ServerPlayer player) {
		return buildStatusMessage(server, player.getUUID(), player.getName().getString());
	}

	public static Component buildStatusMessage(MinecraftServer server, UUID playerUuid, String label) {
		TimeLimitSnapshot snapshot = getSnapshot(server, playerUuid);
		return Component.literal(
			label
				+ " | Category: " + snapshot.category().name()
				+ " | Used: " + formatDuration(snapshot.usedMillis())
				+ " | Remaining: " + formatDuration(snapshot.remainingMillis())
				+ " | Bypass: " + snapshot.bypass()
				+ " | System: " + (snapshot.systemEnabled() ? "enabled" : "disabled")
				+ " | Enforcement: " + (snapshot.enforcementEnabled() ? "enabled" : "disabled")
		);
	}

	public static String formatDuration(long millis) {
		long totalSeconds = Math.max(0L, millis / 1000L);
		long hours = totalSeconds / 3600L;
		long minutes = (totalSeconds % 3600L) / 60L;
		long seconds = totalSeconds % 60L;
		return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
	}

	private static void onServerStarted(MinecraftServer server) {
		zoneId = resolveZoneId();
		storage = TimeLimitStorage.get(server);
		ensureCurrentDate(server, false);
	}

	private static void onServerStopping(MinecraftServer server) {
		long now = System.currentTimeMillis();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			processPlayerUsage(server, player, now);
		}
		SESSION_LAST_UPDATE.clear();
	}

	private static void onPlayerJoin(ServerPlayer player, MinecraftServer server) {
		ensureStorage(server);
		ensureCurrentDate(server, false);
		SESSION_LAST_UPDATE.put(player.getUUID(), System.currentTimeMillis());

		if (shouldEnforce(server, player)) {
			disconnectForLimit(player);
			return;
		}

		if (ModConfig.isTimeLimitSystemEnabled() && !isBypassed(server, player)) {
			TimeLimitSnapshot snapshot = getSnapshot(server, player.getUUID());
			player.sendSystemMessage(Component.literal(
				"Today's remaining time: " + formatDuration(snapshot.remainingMillis()) + " (" + snapshot.category().name() + ")."
			).withStyle(ChatFormatting.AQUA));
		}
	}

	private static void onPlayerDisconnect(ServerPlayer player, MinecraftServer server) {
		processPlayerUsage(server, player, System.currentTimeMillis());
		SESSION_LAST_UPDATE.remove(player.getUUID());
	}

	private static void tick(MinecraftServer server) {
		ensureStorage(server);
		ensureCurrentDate(server, true);

		if (server.getTickCount() % 20 != 0) {
			return;
		}

		long now = System.currentTimeMillis();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			processPlayerUsage(server, player, now);
		}
	}

	private static void processPlayerUsage(MinecraftServer server, ServerPlayer player, long now) {
		PlayerTimeData data = getData(server, player.getUUID());
		long previous = SESSION_LAST_UPDATE.getOrDefault(player.getUUID(), now);
		SESSION_LAST_UPDATE.put(player.getUUID(), now);

		if (!ModConfig.isTimeLimitSystemEnabled() || isBypassed(server, player)) {
			return;
		}

		long delta = Math.max(0L, now - previous);
		if (delta == 0L) {
			return;
		}

		data.addUsedMillis(delta);
		storage.setDirty();
		enforceIfNeeded(server, player);
	}

	private static void enforceIfNeeded(MinecraftServer server, ServerPlayer player) {
		if (shouldEnforce(server, player)) {
			disconnectForLimit(player);
		}
	}

	private static boolean shouldEnforce(MinecraftServer server, ServerPlayer player) {
		if (!ModConfig.isTimeLimitSystemEnabled() || !ModConfig.isTimeLimitEnforcementEnabled() || isBypassed(server, player)) {
			return false;
		}

		PlayerTimeData data = getData(server, player.getUUID());
		long limit = TimeLimitPolicy.getDailyLimitMillis(data.getCategory(), getCurrentDate());
		return data.getUsedMillis() >= limit;
	}

	private static void disconnectForLimit(ServerPlayer player) {
		player.connection.disconnect(Component.literal(ModConfig.getTimeLimitKickMessage()).withStyle(ChatFormatting.RED));
	}

	private static void ensureCurrentDate(MinecraftServer server, boolean announceReset) {
		LocalDate today = getCurrentDate();
		String storedDate = storage.getLastResetDate();
		String currentDate = today.toString();

		if (storedDate.isBlank()) {
			storage.setLastResetDate(currentDate);
			return;
		}

		if (!storedDate.equals(currentDate)) {
			storage.removeAllUsage();
			storage.setLastResetDate(currentDate);
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				SESSION_LAST_UPDATE.put(player.getUUID(), System.currentTimeMillis());
				if (announceReset) {
					player.sendSystemMessage(Component.literal("Your daily play time has been reset for the new day in Bogota.").withStyle(ChatFormatting.GREEN));
				}
			}
		}
	}

	private static LocalDate getCurrentDate() {
		return LocalDate.now(zoneId);
	}

	private static ZoneId resolveZoneId() {
		try {
			return ZoneId.of(ModConfig.getTimeLimitTimezone());
		} catch (DateTimeException ignored) {
			return ZoneId.of("America/Bogota");
		}
	}

	private static boolean hasOpBypass(MinecraftServer server, ServerPlayer player) {
		return ModConfig.isOpBypassValidationsEnabled()
			&& server.getPlayerList().isOp(new NameAndId(player.getGameProfile()));
	}

	private static void ensureStorage(MinecraftServer server) {
		if (storage == null) {
			storage = TimeLimitStorage.get(server);
		}
	}

	private static PlayerTimeData getData(MinecraftServer server, UUID playerUuid) {
		ensureStorage(server);
		return storage.getOrCreate(playerUuid);
	}
}
