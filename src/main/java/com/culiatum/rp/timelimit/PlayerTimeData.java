package com.culiatum.rp.timelimit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class PlayerTimeData {
	public static final Codec<PlayerTimeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf("category", PlayerTimeCategory.UNPAID.name())
			.forGetter(data -> data.category.name()),
		Codec.LONG.optionalFieldOf("used_millis", 0L)
			.forGetter(PlayerTimeData::getUsedMillis),
		Codec.BOOL.optionalFieldOf("bypass", false)
			.forGetter(PlayerTimeData::hasBypass)
	).apply(instance, PlayerTimeData::new));

	private PlayerTimeCategory category = PlayerTimeCategory.UNPAID;
	private long usedMillis;
	private boolean bypass;

	public PlayerTimeData() {
	}

	private PlayerTimeData(String category, long usedMillis, boolean bypass) {
		this.category = PlayerTimeCategory.fromString(category);
		this.usedMillis = Math.max(0L, usedMillis);
		this.bypass = bypass;
	}

	public PlayerTimeCategory getCategory() {
		return category;
	}

	public void setCategory(PlayerTimeCategory category) {
		this.category = category;
	}

	public long getUsedMillis() {
		return usedMillis;
	}

	public void addUsedMillis(long millis) {
		usedMillis += Math.max(0L, millis);
	}

	public void resetUsage() {
		usedMillis = 0L;
	}

	public boolean hasBypass() {
		return bypass;
	}

	public void setBypass(boolean bypass) {
		this.bypass = bypass;
	}
}
