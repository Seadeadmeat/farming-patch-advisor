package com.farmingpatchadvisor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ChatMessageType;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
@Singleton
final class PatchTimerManager
{
	private static final String CONFIG_GROUP = "farming-patch-advisor";
	private static final String CONFIG_KEY = "patchTimers";
	private static final Pattern GROWTH_STAGE = Pattern.compile("(?<!\\d)(\\d+)\\s*/\\s*(\\d+)(?!\\d)");
	private static final long INSPECTION_TIMEOUT_MILLIS = 10_000L;
	private static final int SAME_PATCH_DISTANCE = 1;
	private static final int COMPLETION_DISTANCE = 4;

	private final Client client;
	private final ConfigManager configManager;
	private final FarmingPatchAdvisorPlugin plugin;
	private final Map<String, PatchTimer> timers = new LinkedHashMap<>();
	private Inspection inspection;

	@Inject
	private PatchTimerManager(Client client, ConfigManager configManager, FarmingPatchAdvisorPlugin plugin)
	{
		this.client = client;
		this.configManager = configManager;
		this.plugin = plugin;
	}

	synchronized void load()
	{
		timers.clear();
		String stored = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
		if (stored == null || stored.isEmpty())
		{
			return;
		}

		for (String record : stored.split(";"))
		{
			try
			{
				String[] fields = record.split(",");
				WorldPoint location = new WorldPoint(
					Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
				PatchType patchType = PatchType.valueOf(fields[3]);
				Crop crop = CropCatalog.findByItemId(Integer.parseInt(fields[4]));
				if (crop == null)
				{
					continue;
				}
				boolean plantedTimer = fields.length < 8 || "P".equals(fields[7]);
				int observedStage = fields.length >= 10 ? Integer.parseInt(fields[8]) : -1;
				int totalStages = fields.length >= 10 ? Integer.parseInt(fields[9]) : -1;
				PatchTimer timer = new PatchTimer(location, patchType, crop,
					Instant.ofEpochMilli(Long.parseLong(fields[5])),
					Instant.ofEpochMilli(Long.parseLong(fields[6])), plantedTimer, observedStage, totalStages);
				timers.put(timer.key(), timer);
			}
			catch (RuntimeException ex)
			{
				log.debug("Ignoring malformed farming patch timer", ex);
			}
		}
	}

	synchronized Collection<PatchTimer> getTimers()
	{
		List<PatchTimer> sorted = new ArrayList<>(timers.values());
		sorted.sort(Comparator.comparingInt(PatchLocationCatalog::routeRank)
			.thenComparing(timer -> PatchLocationCatalog.name(timer.getPatchLocation()))
			.thenComparing(PatchTimer::getReadyAt));
		return sorted;
	}

	@Subscribe
	public synchronized void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (isCompletionOption(event.getMenuOption()) && isGameObjectAction(event.getMenuAction()))
		{
			removeCompletedPatch(event);
			return;
		}

		if ("Inspect".equalsIgnoreCase(event.getMenuOption()) && isGameObjectAction(event.getMenuAction()))
		{
			beginInspection(event);
			return;
		}

		if (event.getMenuAction() != MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
		{
			return;
		}

		Crop crop = CropCatalog.findByItemId(event.getItemId());
		Widget selected = client.getSelectedWidget();
		if (crop == null && selected != null)
		{
			crop = CropCatalog.findByItemId(selected.getItemId());
		}
		if (crop == null)
		{
			return;
		}

		ObjectComposition composition = client.getObjectDefinition(event.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		PatchType patchType = composition == null ? null : PatchClassifier.classify(composition.getName());
		if (patchType == null || patchType != crop.getPatchType())
		{
			return;
		}

		WorldPoint clicked = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		PatchTimer nearby = findNearbyTimer(anchor, patchType, SAME_PATCH_DISTANCE);
		if (nearby != null && !nearby.isPlantedTimer())
		{
			timers.remove(nearby.key());
		}
		Instant plantedAt = Instant.now();
		PatchTimer timer = new PatchTimer(anchor, patchType, crop, plantedAt,
			plantedAt.plus(CropGrowthTimes.forCrop(crop)));
		timers.put(timer.key(), timer);
		save();
		log.debug("Started farming timer for {} at {}", crop.getName(), anchor);
	}

	synchronized void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}
		if (inspection == null || System.currentTimeMillis() - inspection.startedAtMillis > INSPECTION_TIMEOUT_MILLIS)
		{
			inspection = null;
			return;
		}

		String message = event.getMessage().replaceAll("<[^>]+>", "");
		Crop mentioned = CropCatalog.findInText(message, inspection.patchType);
		if (mentioned != null)
		{
			inspection.crop = mentioned;
		}
		Matcher matcher = GROWTH_STAGE.matcher(message);
		if (matcher.find())
		{
			inspection.currentStage = Integer.parseInt(matcher.group(1));
			inspection.totalStages = Integer.parseInt(matcher.group(2));
		}
		if (inspection.crop != null && inspection.currentStage != null && inspection.totalStages != null)
		{
			applyInspection();
			inspection = null;
		}
	}

	synchronized void clear()
	{
		timers.clear();
		save();
	}

	synchronized void reset(String locationName, PatchType patchType)
	{
		timers.values().removeIf(timer -> timer.getPatchType() == patchType
			&& PatchLocationCatalog.name(timer.getPatchLocation()).equals(locationName));
		save();
	}

	private void save()
	{
		StringBuilder value = new StringBuilder();
		for (PatchTimer timer : timers.values())
		{
			if (value.length() > 0)
			{
				value.append(';');
			}
			WorldPoint point = timer.getPatchLocation();
			value.append(point.getX()).append(',')
				.append(point.getY()).append(',')
				.append(point.getPlane()).append(',')
				.append(timer.getPatchType().name()).append(',')
				.append(timer.getCrop().getItemId()).append(',')
				.append(timer.getPlantedAt().toEpochMilli()).append(',')
				.append(timer.getReadyAt().toEpochMilli()).append(',')
				.append(timer.isPlantedTimer() ? 'P' : 'I').append(',')
				.append(timer.getObservedStage()).append(',')
				.append(timer.getTotalStages());
		}
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY, value.toString());
	}

	private void beginInspection(MenuOptionClicked event)
	{
		ObjectComposition composition = client.getObjectDefinition(event.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		PatchType patchType = composition == null ? null : PatchClassifier.classifyGrowing(composition.getName());
		if (patchType == null)
		{
			return;
		}
		WorldPoint clicked = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		Crop crop = CropCatalog.findInText(composition.getName(), patchType);
		inspection = new Inspection(anchor, patchType, crop, System.currentTimeMillis());
	}

	private void removeCompletedPatch(MenuOptionClicked event)
	{
		ObjectComposition composition = client.getObjectDefinition(event.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		PatchType patchType = composition == null ? null : PatchClassifier.classifyGrowing(composition.getName());
		if (patchType == null)
		{
			return;
		}
		WorldPoint clicked = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		PatchTimer timer = findNearbyTimer(clicked, patchType, COMPLETION_DISTANCE);
		if (timer != null)
		{
			timers.remove(timer.key());
			save();
		}
	}

	private void applyInspection()
	{
		String key = inspection.location.getX() + ":" + inspection.location.getY() + ":"
			+ inspection.location.getPlane() + ":" + inspection.patchType.name();
		PatchTimer existing = timers.get(key);
		if (existing == null)
		{
			existing = findNearbyTimer(inspection.location, inspection.patchType, SAME_PATCH_DISTANCE);
		}
		if (existing != null && existing.isPlantedTimer())
		{
			return;
		}
		try
		{
			Instant observedAt = Instant.now();
			WorldPoint location = existing == null ? inspection.location : existing.getPatchLocation();
			if (existing != null)
			{
				timers.remove(existing.key());
			}
			PatchTimer timer = new PatchTimer(location, inspection.patchType, inspection.crop,
				observedAt, observedAt.plus(CropGrowthTimes.maximumRemainingAtStage(
					inspection.crop, inspection.currentStage, inspection.totalStages)),
				false, inspection.currentStage, inspection.totalStages);
			timers.put(timer.key(), timer);
			save();
		}
		catch (IllegalArgumentException ex)
		{
			log.debug("Ignoring invalid inspected farming stage", ex);
		}
	}

	private PatchTimer findNearbyTimer(WorldPoint location, PatchType patchType, int maximumDistance)
	{
		for (PatchTimer timer : timers.values())
		{
			WorldPoint point = timer.getPatchLocation();
			if (timer.getPatchType() == patchType && point.getPlane() == location.getPlane()
				&& Math.max(Math.abs(point.getX() - location.getX()), Math.abs(point.getY() - location.getY())) <= maximumDistance)
			{
				return timer;
			}
		}
		return null;
	}

	private static boolean isGameObjectAction(MenuAction action)
	{
		return action == MenuAction.GAME_OBJECT_FIRST_OPTION || action == MenuAction.GAME_OBJECT_SECOND_OPTION
			|| action == MenuAction.GAME_OBJECT_THIRD_OPTION || action == MenuAction.GAME_OBJECT_FOURTH_OPTION
			|| action == MenuAction.GAME_OBJECT_FIFTH_OPTION;
	}

	private static boolean isCompletionOption(String option)
	{
		String normalized = option == null ? "" : option.toLowerCase();
		return normalized.equals("pick") || normalized.equals("pick-fruit") || normalized.equals("harvest")
			|| normalized.equals("check-health");
	}

	private static final class Inspection
	{
		private final WorldPoint location;
		private final PatchType patchType;
		private final long startedAtMillis;
		private Crop crop;
		private Integer currentStage;
		private Integer totalStages;

		private Inspection(WorldPoint location, PatchType patchType, Crop crop, long startedAtMillis)
		{
			this.location = location;
			this.patchType = patchType;
			this.crop = crop;
			this.startedAtMillis = startedAtMillis;
		}
	}
}
