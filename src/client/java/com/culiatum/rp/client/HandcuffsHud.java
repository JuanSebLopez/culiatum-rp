package com.culiatum.rp.client;

import com.culiatum.rp.item.ModItems;
import com.culiatum.rp.police.HandcuffsManager;
import com.culiatum.rp.police.HandcuffsTargeting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class HandcuffsHud {
	private static UUID currentTargetUuid;
	private static long startedAtTick = -1L;
	private static float progress;

	private HandcuffsHud() {
	}

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null || client.level == null || !player.isUsingItem() || !player.getActiveItem().is(ModItems.HANDCUFFS)) {
			reset();
			return;
		}

		Player target = HandcuffsTargeting.findTarget(player, HandcuffsManager.RANGE);
		if (target == null) {
			reset();
			return;
		}

		if (currentTargetUuid == null || !currentTargetUuid.equals(target.getUUID())) {
			currentTargetUuid = target.getUUID();
			startedAtTick = client.level.getGameTime();
			progress = 0.0F;
			return;
		}

		long elapsedTicks = Math.max(0L, client.level.getGameTime() - startedAtTick);
		progress = Math.min(1.0F, elapsedTicks / (float) HandcuffsManager.CUFF_DURATION_TICKS);
	}

	public static void render(GuiGraphics drawContext, DeltaTracker tickCounter) {
		if (progress <= 0.0F) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		int barWidth = 90;
		int barHeight = 8;
		int x = (screenWidth - barWidth) / 2;
		int y = screenHeight / 2 + 18;
		int fillWidth = Math.max(0, Math.min(barWidth, Math.round(barWidth * progress)));

		drawContext.fill(x - 1, y - 1, x + barWidth + 1, y + barHeight + 1, 0xAA000000);
		drawContext.fill(x, y, x + barWidth, y + barHeight, 0x660F172A);
		drawContext.fill(x, y, x + fillWidth, y + barHeight, 0xFFBFA100);
		drawContext.drawCenteredString(client.font, Component.literal("Detaining...").withStyle(ChatFormatting.YELLOW), screenWidth / 2, y - 10, 0xFFFFFF);
	}

	private static void reset() {
		currentTargetUuid = null;
		startedAtTick = -1L;
		progress = 0.0F;
	}
}
