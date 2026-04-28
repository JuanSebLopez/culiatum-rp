package com.culiatum.rp.timelimit;

import com.culiatum.rp.ModConfig;

import java.time.DayOfWeek;
import java.time.LocalDate;

public final class TimeLimitPolicy {
	private TimeLimitPolicy() {
	}

	public static long getDailyLimitMillis(PlayerTimeCategory category, LocalDate date) {
		boolean weekend = isWeekend(date.getDayOfWeek());
		int seconds;

		if (category == PlayerTimeCategory.PAID) {
			seconds = weekend ? ModConfig.getWeekendPaidSeconds() : ModConfig.getWeekdayPaidSeconds();
		} else {
			seconds = weekend ? ModConfig.getWeekendUnpaidSeconds() : ModConfig.getWeekdayUnpaidSeconds();
		}

		return seconds * 1000L;
	}

	private static boolean isWeekend(DayOfWeek dayOfWeek) {
		return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}
}
