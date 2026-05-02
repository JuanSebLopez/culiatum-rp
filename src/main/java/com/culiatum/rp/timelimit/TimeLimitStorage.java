package com.culiatum.rp.timelimit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimeLimitStorage extends SavedData {
	private static final String DATA_NAME = "culiatum_rp_time_limits";
	private static final Codec<UUID> UUID_CODEC = Codec.STRING.xmap(UUID::fromString, UUID::toString);
	private static final Codec<TimeLimitStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("last_reset_date", "")
			.forGetter(TimeLimitStorage::getLastResetDate),
		Codec.unboundedMap(UUID_CODEC, PlayerTimeData.CODEC)
			.optionalFieldOf("players", Map.of())
			.forGetter(TimeLimitStorage::getPlayerData)
	).apply(instance, TimeLimitStorage::new));
	private static final SavedDataType<TimeLimitStorage> DATA_TYPE =
		new SavedDataType<>(DATA_NAME, TimeLimitStorage::new, CODEC, DataFixTypes.LEVEL);

	private String lastResetDate = "";
	private final Map<UUID, PlayerTimeData> playerData = new HashMap<>();

	public TimeLimitStorage() {
	}

	public static TimeLimitStorage get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(DATA_TYPE);
	}

	private TimeLimitStorage(String lastResetDate, Map<UUID, PlayerTimeData> playerData) {
		this.lastResetDate = lastResetDate;
		this.playerData.putAll(playerData);
	}

	public String getLastResetDate() {
		return lastResetDate;
	}

	public void setLastResetDate(String lastResetDate) {
		this.lastResetDate = lastResetDate;
		setDirty();
	}

	public Map<UUID, PlayerTimeData> getPlayerData() {
		return playerData;
	}

	public PlayerTimeData getOrCreate(UUID uuid) {
		PlayerTimeData data = playerData.computeIfAbsent(uuid, ignored -> new PlayerTimeData());
		setDirty();
		return data;
	}

	public void removeAllUsage() {
		for (PlayerTimeData data : playerData.values()) {
			data.resetUsage();
		}

		setDirty();
	}
}
