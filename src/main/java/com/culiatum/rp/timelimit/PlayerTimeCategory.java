package com.culiatum.rp.timelimit;

public enum PlayerTimeCategory {
	PAID,
	UNPAID;

	public static PlayerTimeCategory fromString(String value) {
		for (PlayerTimeCategory category : values()) {
			if (category.name().equalsIgnoreCase(value)) {
				return category;
			}
		}

		return UNPAID;
	}
}
