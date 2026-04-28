package com.culiatum.rp.vote;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimeVoteManager {
	private static final int VOTE_DURATION_TICKS = 20 * 30;
	private static final long DAY_TIME = 1000L;
	private static final long NIGHT_TIME = 13000L;
	private static final String BUTTON_PADDING = "          ";

	private static VoteSession activeVote;

	private TimeVoteManager() {
	}

	public static int startVote(ServerPlayer initiator, VoteTarget target) {
		if (activeVote != null) {
			initiator.sendSystemMessage(Component.literal("There is already an active world vote."));
			return 0;
		}

		MinecraftServer server = initiator.level().getServer();
		if (server == null) {
			return 0;
		}

		if (isAlreadyTargetTime(server, target)) {
			initiator.sendSystemMessage(Component.literal("It is already " + target.displayName + "."));
			return 0;
		}

		activeVote = new VoteSession(target, server.getTickCount() + VOTE_DURATION_TICKS);
		activeVote.votes.put(initiator.getUUID(), true);

		broadcastVoteStarted(server, initiator, target);
		evaluateVote(server);
		return 1;
	}

	public static int castVote(ServerPlayer voter, boolean approve) {
		return castVote(voter, null, approve);
	}

	public static int castVote(ServerPlayer voter, VoteTarget expectedTarget, boolean approve) {
		if (activeVote == null) {
			voter.sendSystemMessage(Component.literal("There is no active world vote right now."));
			return 0;
		}

		if (expectedTarget != null && activeVote.target != expectedTarget) {
			voter.sendSystemMessage(Component.literal("That vote is no longer active."));
			return 0;
		}

		activeVote.votes.put(voter.getUUID(), approve);
		voter.sendSystemMessage(Component.literal("You voted " + (approve ? "yes" : "no") + "."));
		evaluateVote(voter.level().getServer());
		return 1;
	}

	public static void tick(MinecraftServer server) {
		if (activeVote == null) {
			return;
		}

		if (server.getTickCount() >= activeVote.expiresAtTick) {
			resolveExpiredVote(server);
		}
	}

	private static void evaluateVote(MinecraftServer server) {
		if (activeVote == null || server == null) {
			return;
		}

		int eligiblePlayers = getEligiblePlayerCount(server);
		int yesVotes = activeVote.countVotes(true);
		int noVotes = activeVote.countVotes(false);

		if (yesVotes > eligiblePlayers / 2) {
			applyVoteResult(server, activeVote.target);
			broadcast(server, Component.literal(activeVote.target.successMessage()).withStyle(ChatFormatting.GREEN));
			activeVote = null;
			return;
		}

		if (noVotes > eligiblePlayers / 2) {
			broadcast(server, Component.literal("Vote failed. The time will stay the same.").withStyle(ChatFormatting.RED));
			activeVote = null;
		}
	}

	private static void resolveExpiredVote(MinecraftServer server) {
		if (activeVote == null) {
			return;
		}

		int eligiblePlayers = getEligiblePlayerCount(server);
		int yesVotes = activeVote.countVotes(true);

		if (yesVotes > eligiblePlayers / 2) {
			applyVoteResult(server, activeVote.target);
			broadcast(server, Component.literal(activeVote.target.successMessage()).withStyle(ChatFormatting.GREEN));
		} else {
			broadcast(server, Component.literal("Vote expired without enough yes votes.").withStyle(ChatFormatting.RED));
		}

		activeVote = null;
	}

	private static void applyVoteResult(MinecraftServer server, VoteTarget target) {
		ServerLevel overworld = server.overworld();
		if (target == VoteTarget.CLEAR_WEATHER) {
			overworld.setWeatherParameters(0, 0, false, false);
			return;
		}

		long currentTime = overworld.getDayTime();
		long nextTime = getNextTime(currentTime, target == VoteTarget.DAY ? DAY_TIME : NIGHT_TIME);
		overworld.setDayTime(nextTime);
	}

	private static boolean isAlreadyTargetTime(MinecraftServer server, VoteTarget target) {
		if (target == VoteTarget.CLEAR_WEATHER) {
			ServerLevel overworld = server.overworld();
			return !overworld.isRaining() && !overworld.isThundering();
		}

		long timeOfDay = Math.floorMod(server.overworld().getDayTime(), 24000L);
		boolean isDay = timeOfDay < 12000L;
		return target == VoteTarget.DAY ? isDay : !isDay;
	}

	private static long getNextTime(long currentTime, long targetTimeOfDay) {
		long currentTimeOfDay = Math.floorMod(currentTime, 24000L);
		long dayBase = currentTime - currentTimeOfDay;

		if (targetTimeOfDay <= currentTimeOfDay) {
			return dayBase + 24000L + targetTimeOfDay;
		}

		return dayBase + targetTimeOfDay;
	}

	private static int getEligiblePlayerCount(MinecraftServer server) {
		return (int) server.getPlayerList().getPlayers().stream()
			.filter(player -> !player.isSpectator())
			.count();
	}

	private static void broadcastVoteStarted(MinecraftServer server, ServerPlayer initiator, VoteTarget target) {
		MutableComponent message = Component.literal("")
			.append(Component.literal("\n"))
			.append(Component.literal("        --- World Vote ---\n").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD))
			.append(Component.literal(initiator.getName().getString()).withStyle(ChatFormatting.GOLD))
			.append(Component.literal(" wants to "))
			.append(Component.literal(target.voteDescription()).withStyle(ChatFormatting.AQUA))
			.append(Component.literal(".\n"))
			.append(Component.literal(BUTTON_PADDING))
			.append(createVoteButton("YES (Y)", target.yesCommand(), ChatFormatting.GREEN))
			.append(Component.literal("    "))
			.append(createVoteButton("NO (N)", target.noCommand(), ChatFormatting.RED))
			.append(Component.literal("\n"))
			.append(Component.literal("            Expires in 30s").withStyle(ChatFormatting.GRAY));

		broadcast(server, message);
	}

	private static MutableComponent createVoteButton(String label, String command, ChatFormatting color) {
		return Component.literal("[" + label + "]").withStyle(style -> style
			.withColor(color)
			.withBold(true)
			.withClickEvent(new ClickEvent.RunCommand(command))
			.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to vote"))));
	}

	private static void broadcast(MinecraftServer server, Component message) {
		server.getPlayerList().broadcastSystemMessage(message, false);
	}

	public enum VoteTarget {
		DAY("day", "make it day", "Vote passed. It is now day."),
		NIGHT("night", "make it night", "Vote passed. It is now night."),
		CLEAR_WEATHER("clear weather", "clear the weather", "Vote passed. The weather is now clear.");

		private final String displayName;
		private final String voteDescription;
		private final String successMessage;

		VoteTarget(String displayName, String voteDescription, String successMessage) {
			this.displayName = displayName;
			this.voteDescription = voteDescription;
			this.successMessage = successMessage;
		}

		private String voteDescription() {
			return voteDescription;
		}

		private String successMessage() {
			return successMessage;
		}

		private String yesCommand() {
			return switch (this) {
				case DAY -> "/vote day yes";
				case NIGHT -> "/vote night yes";
				case CLEAR_WEATHER -> "/vote clearweather yes";
			};
		}

		private String noCommand() {
			return switch (this) {
				case DAY -> "/vote day no";
				case NIGHT -> "/vote night no";
				case CLEAR_WEATHER -> "/vote clearweather no";
			};
		}
	}

	private static final class VoteSession {
		private final VoteTarget target;
		private final int expiresAtTick;
		private final Map<UUID, Boolean> votes = new HashMap<>();

		private VoteSession(VoteTarget target, int expiresAtTick) {
			this.target = target;
			this.expiresAtTick = expiresAtTick;
		}

		private int countVotes(boolean value) {
			return (int) votes.values().stream()
				.filter(vote -> vote == value)
				.count();
		}
	}
}
