package com.culiatum.rp.timelimit;

public record TimeLimitSnapshot(
	PlayerTimeCategory category,
	long usedMillis,
	long limitMillis,
	long remainingMillis,
	boolean bypass,
	boolean systemEnabled,
	boolean enforcementEnabled
) {
}
