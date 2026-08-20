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
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
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
	private static final Pattern GROWTH_STAGE = Pattern.compile(
		"(?i)(?:growth\\s+stage\\s*)?(?<!\\d)(\\d+)\\s*(?:/|of)\\s*(\\d+)(?!\\d)");
	private static final long INSPECTION_TIMEOUT_MILLIS = 10_000L;
	private static final int SAME_PATCH_DISTANCE = 1;
	private static final int COMPLETION_DISTANCE = 4;

	private final Client client;
	private final ConfigManager configManager;
	private final FarmingPatchAdvisorConfig config;
	private final FarmingPatchAdvisorPlugin plugin;
	private final Map<String, PatchTimer> timers = new LinkedHashMap<>();
	private final Map<String, CareState> pendingCare = new LinkedHashMap<>();
	private final Map<String, PendingCareAction> pendingCareActions = new LinkedHashMap<>();
	private Inspection inspection;

	@Inject
	private PatchTimerManager(Client client, ConfigManager configManager, FarmingPatchAdvisorPlugin plugin,
		FarmingPatchAdvisorConfig config)
	{
		this.client = client;
		this.configManager = configManager;
		this.plugin = plugin;
		this.config = config;
	}

	synchronized void load()
	{
		unload();
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
		String stored = configManager.getRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY);
		if (stored == null)
		{
			stored = configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY);
			if (stored != null)
			{
				configManager.setRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY, stored);
				configManager.unsetConfiguration(CONFIG_GROUP, CONFIG_KEY);
			}
		}
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
				boolean dead = fields.length >= 11 && "D".equals(fields[10]);
				boolean diseased = fields.length >= 11 && "S".equals(fields[10]);
				boolean compostApplied = fields.length >= 12 && "C".equals(fields[11]);
				boolean watered = fields.length >= 13 && "W".equals(fields[12]);
				PatchTimer timer = new PatchTimer(location, patchType, crop,
					Instant.ofEpochMilli(Long.parseLong(fields[5])),
					Instant.ofEpochMilli(Long.parseLong(fields[6])), plantedTimer, observedStage,
					totalStages, dead, diseased, compostApplied, watered);
				timers.put(timer.key(), timer);
			}
			catch (RuntimeException ex)
			{
				log.debug("Ignoring malformed farming patch timer", ex);
			}
		}
	}

	synchronized void unload()
	{
		timers.clear();
		pendingCare.clear();
		pendingCareActions.clear();
		inspection = null;
	}

	synchronized Collection<PatchTimer> getTimers()
	{
		List<PatchTimer> sorted = new ArrayList<>();
		for (PatchTimer timer : timers.values())
		{
			if (PatchLocationSelection.isEnabled(config, PatchLocationCatalog.name(timer.getPatchLocation())))
			{
				sorted.add(timer);
			}
		}
		sorted.sort(Comparator.comparingInt(PatchLocationCatalog::routeRank)
			.thenComparing(timer -> PatchLocationCatalog.name(timer.getPatchLocation()))
			.thenComparing(PatchTimer::getReadyAt));
		return sorted;
	}

	synchronized boolean hasDiseasedPatch(boolean secateursOnly)
	{
		for (PatchTimer timer : timers.values())
		{
			if (timer.isDiseased() && (!secateursOnly || PatchRemedy.usesSecateurs(timer.getPatchType())))
			{
				return true;
			}
		}
		return false;
	}

	synchronized PatchTimer findTimer(WorldPoint location, PatchType patchType, int maximumDistance)
	{
		return findNearbyTimer(location, patchType, maximumDistance);
	}

	synchronized PatchTimer findTimer(WorldPoint location, int maximumDistance)
	{
		return findNearestTimer(location, maximumDistance);
	}

	@Subscribe
	public synchronized void onMenuOptionClicked(MenuOptionClicked event)
	{
		if ("Check-health".equalsIgnoreCase(event.getMenuOption()) && isGameObjectAction(event.getMenuAction()))
		{
			markCompletedPatch(event);
			return;
		}
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
		if (recordCareAction(event))
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
		if (!PatchClassifier.isFarmRunPatchObject(event.getId()))
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
		if (!PatchLocationSelection.isEnabled(config, PatchLocationCatalog.name(clicked)))
		{
			return;
		}
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		PatchTimer nearby = findNearbyTimer(anchor, patchType, SAME_PATCH_DISTANCE);
		if (nearby != null && (!nearby.isPlantedTimer() || nearby.isDead() || nearby.isDiseased()))
		{
			timers.remove(nearby.key());
		}
		Instant plantedAt = Instant.now();
		CareState care = pendingCare.remove(timerKey(anchor, patchType));
		PatchTimer timer = new PatchTimer(anchor, patchType, crop, plantedAt,
			plantedAt.plus(CropGrowthTimes.forCrop(crop)), true, -1, -1, false, false,
			care != null && care.compostApplied, care != null && care.watered);
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
		String message = event.getMessage().replaceAll("<[^>]+>", "");
		boolean compostConfirmed = isCompostedMessage(message);
		boolean waterConfirmed = isWateredMessage(message);
		if (compostConfirmed)
		{
			confirmMostRecentCareAction(true);
		}
		if (waterConfirmed)
		{
			confirmMostRecentCareAction(false);
		}
		if (inspection == null || System.currentTimeMillis() - inspection.startedAtMillis > INSPECTION_TIMEOUT_MILLIS)
		{
			inspection = null;
			return;
		}
		if (compostConfirmed)
		{
			inspection.compostApplied = true;
		}
		if (waterConfirmed)
		{
			inspection.watered = true;
		}
		Crop mentioned = CropCatalog.findInText(message, inspection.patchType);
		if (mentioned != null)
		{
			inspection.crop = mentioned;
		}
		if (isDeadStateMessage(message))
		{
			applyUnhealthyInspection(true);
			inspection = null;
			return;
		}
		if (isDiseasedStateMessage(message))
		{
			applyUnhealthyInspection(false);
			inspection = null;
			return;
		}
		int[] growthStage = parseGrowthStage(message);
		if (growthStage != null)
		{
			inspection.currentStage = growthStage[0];
			inspection.totalStages = growthStage[1];
		}
		if (isCompletedStateMessage(message))
		{
			inspection.completed = true;
		}
		if (inspection.crop != null && (inspection.completed
			|| inspection.currentStage != null && inspection.totalStages != null))
		{
			applyInspection();
			inspection = null;
		}
	}

	synchronized void onPatchObjectSpawned(GameObject object)
	{
		if (!PatchClassifier.isFarmRunPatchObject(object.getId()))
		{
			return;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		String name = composition == null ? "" : composition.getName();
		String[] actions = composition == null ? null : composition.getActions();
		boolean dead = isDeadObjectState(name, actions);
		boolean diseased = isDiseasedObjectState(name, actions);
		boolean watered = isWateredObjectName(name);
		PatchType patchType = PatchClassifier.classifyGrowing(name);
		if (!dead && !diseased && patchType == null)
		{
			return;
		}
		PatchTimer existing = patchType == null
			? findNearestTimer(object.getWorldLocation(), COMPLETION_DISTANCE)
			: findNearbyTimer(object.getWorldLocation(), patchType,
				dead || diseased ? COMPLETION_DISTANCE : SAME_PATCH_DISTANCE);
		if (existing == null)
		{
			return;
		}
		PendingCareAction pending = pendingCareActions.get(existing.key());
		if (pending != null && pending.watered
			&& (watered || composition != null && composition.getId() != pending.objectStateId))
		{
			confirmCareAction(existing.key(), false);
			existing = timers.get(existing.key());
		}
		if (dead && !existing.isDead())
		{
			timers.put(existing.key(), unhealthyTimer(existing, true));
			save();
		}
		else if (diseased && !existing.isDiseased())
		{
			timers.put(existing.key(), unhealthyTimer(existing, false));
			save();
		}
		else if (!dead && !diseased && (existing.isDead() || existing.isDiseased()))
		{
			timers.put(existing.key(), healthyTimer(existing));
			save();
		}
		else if (watered && !existing.isWatered())
		{
			timers.put(existing.key(), withCare(existing, existing.isCompostApplied(), true));
			save();
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
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
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
				.append(timer.getTotalStages()).append(',')
				.append(timer.isDead() ? 'D' : timer.isDiseased() ? 'S' : 'A').append(',')
				.append(timer.isCompostApplied() ? 'C' : 'N').append(',')
				.append(timer.isWatered() ? 'W' : 'N');
		}
		configManager.setRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY, value.toString());
	}

	private void beginInspection(MenuOptionClicked event)
	{
		if (!PatchClassifier.isFarmRunPatchObject(event.getId()))
		{
			return;
		}
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
		if (!PatchLocationSelection.isEnabled(config, PatchLocationCatalog.name(clicked)))
		{
			return;
		}
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		Crop crop = CropCatalog.findInText(composition.getName(), patchType);
		if (crop == null)
		{
			PatchTimer existing = findNearbyTimer(anchor, patchType, COMPLETION_DISTANCE);
			if (existing != null)
			{
				crop = existing.getCrop();
			}
		}
		inspection = new Inspection(anchor, patchType, crop, System.currentTimeMillis());
	}

	private void removeCompletedPatch(MenuOptionClicked event)
	{
		if (!PatchClassifier.isFarmRunPatchObject(event.getId()))
		{
			return;
		}
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
		if (!PatchLocationSelection.isEnabled(config, PatchLocationCatalog.name(clicked)))
		{
			return;
		}
		PatchTimer timer = findNearbyTimer(clicked, patchType, COMPLETION_DISTANCE);
		if (timer != null)
		{
			timers.remove(timer.key());
			save();
		}
	}

	private boolean recordCareAction(MenuOptionClicked event)
	{
		int itemId = event.getItemId();
		Widget selected = client.getSelectedWidget();
		if (itemId < 0 && selected != null)
		{
			itemId = selected.getItemId();
		}
		boolean compost = isCompostItem(itemId);
		boolean water = isWateringCan(itemId);
		if (!compost && !water)
		{
			return false;
		}

		ObjectComposition composition = client.getObjectDefinition(event.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		PatchType patchType = composition == null ? null : PatchClassifier.classifyGrowing(composition.getName());
		if (patchType == null || water && !patchType.canBeWatered())
		{
			return true;
		}
		WorldPoint clicked = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		PatchTimer existing = findNearbyTimer(anchor, patchType, SAME_PATCH_DISTANCE);
		String key = existing == null ? timerKey(anchor, patchType) : existing.key();
		PendingCareAction action = pendingCareActions.computeIfAbsent(key,
			ignored -> new PendingCareAction());
		action.compostApplied |= compost;
		action.watered |= water;
		action.objectStateId = composition == null ? event.getId() : composition.getId();
		action.startedAtMillis = System.currentTimeMillis();
		return true;
	}

	private void confirmMostRecentCareAction(boolean compost)
	{
		String latestKey = null;
		long latestTime = 0;
		long now = System.currentTimeMillis();
		for (Map.Entry<String, PendingCareAction> entry : pendingCareActions.entrySet())
		{
			PendingCareAction action = entry.getValue();
			boolean matches = compost ? action.compostApplied : action.watered;
			if (matches && now - action.startedAtMillis <= 30_000L && action.startedAtMillis >= latestTime)
			{
				latestKey = entry.getKey();
				latestTime = action.startedAtMillis;
			}
		}
		if (latestKey != null)
		{
			confirmCareAction(latestKey, compost);
		}
	}

	private void confirmCareAction(String key, boolean compost)
	{
		PendingCareAction action = pendingCareActions.get(key);
		if (action == null)
		{
			return;
		}
		PatchTimer timer = timers.get(key);
		if (timer != null)
		{
			timers.put(key, withCare(timer, timer.isCompostApplied() || compost,
				timer.isWatered() || !compost));
			save();
		}
		else
		{
			CareState care = pendingCare.computeIfAbsent(key, ignored -> new CareState());
			care.compostApplied |= compost;
			care.watered |= !compost;
		}
		if (compost)
		{
			action.compostApplied = false;
		}
		else
		{
			action.watered = false;
		}
		if (!action.compostApplied && !action.watered)
		{
			pendingCareActions.remove(key);
		}
	}

	private void markCompletedPatch(MenuOptionClicked event)
	{
		ObjectComposition composition = client.getObjectDefinition(event.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		String name = composition == null ? "" : composition.getName();
		PatchType patchType = PatchClassifier.classifyGrowing(name);
		if (patchType == null)
		{
			return;
		}
		WorldPoint clicked = WorldPoint.fromScene(client, event.getParam0(), event.getParam1(), client.getPlane());
		WorldPoint anchor = plugin.getPatchAnchor(patchType, clicked);
		PatchTimer existing = findNearbyTimer(anchor, patchType, COMPLETION_DISTANCE);
		Crop crop = existing == null ? CropCatalog.findInText(name, patchType) : existing.getCrop();
		if (crop == null)
		{
			return;
		}
		Instant now = Instant.now();
		PatchTimer completed = new PatchTimer(existing == null ? anchor : existing.getPatchLocation(),
			patchType, crop, existing == null ? now : existing.getPlantedAt(), now,
			existing != null && existing.isPlantedTimer(), 1, 1, false, false,
			existing != null && existing.isCompostApplied(), existing != null && existing.isWatered());
		if (existing != null)
		{
			timers.remove(existing.key());
		}
		timers.put(completed.key(), completed);
		save();
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
		if (existing != null && (existing.isDead() || existing.isDiseased()))
		{
			existing = healthyTimer(existing);
			timers.put(existing.key(), existing);
		}
		Instant observedAt = Instant.now();
		if (existing != null && existing.isPlantedTimer() && !inspection.completed
			&& observedAt.isBefore(existing.getReadyAt()))
		{
			if (inspection.compostApplied || inspection.watered)
			{
				timers.put(existing.key(), withCare(existing,
					existing.isCompostApplied() || inspection.compostApplied,
					existing.isWatered() || inspection.watered));
				save();
			}
			return;
		}
		try
		{
			WorldPoint location = existing == null ? inspection.location : existing.getPatchLocation();
			if (existing != null)
			{
				timers.remove(existing.key());
			}
			int currentStage = inspection.completed ? 1 : inspection.currentStage;
			int totalStages = inspection.completed ? 1 : inspection.totalStages;
			Instant readyAt = inspection.completed ? observedAt : observedAt.plus(
				CropGrowthTimes.maximumRemainingAtStage(inspection.crop, currentStage, totalStages));
			PatchTimer timer = new PatchTimer(location, inspection.patchType, inspection.crop,
				observedAt, readyAt, false, currentStage, totalStages, false, false,
				inspection.compostApplied || existing != null && existing.isCompostApplied(),
				inspection.watered || existing != null && existing.isWatered());
			timers.put(timer.key(), timer);
			save();
		}
		catch (IllegalArgumentException ex)
		{
			log.debug("Ignoring invalid inspected farming stage", ex);
		}
	}

	private void applyUnhealthyInspection(boolean dead)
	{
		PatchTimer existing = findNearbyTimer(inspection.location, inspection.patchType, SAME_PATCH_DISTANCE);
		Crop crop = inspection.crop != null ? inspection.crop : existing == null ? null : existing.getCrop();
		if (crop == null)
		{
			return;
		}
		if (existing != null)
		{
			timers.put(existing.key(), unhealthyTimer(existing, dead));
		}
		else
		{
			Instant now = Instant.now();
			PatchTimer unhealthy = new PatchTimer(inspection.location, inspection.patchType, crop,
				now, now, false, -1, -1, dead, !dead);
			timers.put(unhealthy.key(), unhealthy);
		}
		save();
	}

	private static PatchTimer unhealthyTimer(PatchTimer timer, boolean dead)
	{
		return new PatchTimer(timer.getPatchLocation(), timer.getPatchType(), timer.getCrop(),
			timer.getPlantedAt(), timer.getReadyAt(), timer.isPlantedTimer(),
			timer.getObservedStage(), timer.getTotalStages(), dead, !dead,
			timer.isCompostApplied(), timer.isWatered());
	}

	private static PatchTimer healthyTimer(PatchTimer timer)
	{
		return new PatchTimer(timer.getPatchLocation(), timer.getPatchType(), timer.getCrop(),
			timer.getPlantedAt(), timer.getReadyAt(), timer.isPlantedTimer(),
			timer.getObservedStage(), timer.getTotalStages(), false, false,
			timer.isCompostApplied(), timer.isWatered());
	}

	private static PatchTimer withCare(PatchTimer timer, boolean compostApplied, boolean watered)
	{
		return new PatchTimer(timer.getPatchLocation(), timer.getPatchType(), timer.getCrop(),
			timer.getPlantedAt(), timer.getReadyAt(), timer.isPlantedTimer(),
			timer.getObservedStage(), timer.getTotalStages(), timer.isDead(), timer.isDiseased(),
			compostApplied, watered);
	}

	static boolean isDeadStateMessage(String message)
	{
		String normalized = message == null ? "" : message.toLowerCase();
		return normalized.contains("has died") || normalized.contains("is dead")
			|| normalized.contains("dead plant");
	}

	static boolean isDiseasedStateMessage(String message)
	{
		String normalized = message == null ? "" : message.toLowerCase();
		return normalized.contains("has become diseased") || normalized.contains("is diseased")
			|| normalized.contains("diseased plant") || normalized.contains("diseased leaves");
	}

	static boolean isCompletedStateMessage(String message)
	{
		String normalized = message == null ? "" : message.toLowerCase();
		return normalized.contains("fully grown") || normalized.contains("ready to harvest")
			|| normalized.contains("ready for harvesting") || normalized.contains("check its health");
	}

	static int[] parseGrowthStage(String message)
	{
		Matcher matcher = GROWTH_STAGE.matcher(message == null ? "" : message);
		return matcher.find() ? new int[]{Integer.parseInt(matcher.group(1)),
			Integer.parseInt(matcher.group(2))} : null;
	}

	static boolean isCompostedMessage(String message)
	{
		String normalized = message == null ? "" : message.toLowerCase();
		return normalized.startsWith("you treat the ") && normalized.contains("compost")
			|| normalized.contains("soil has been treated with") && normalized.contains("compost")
			|| normalized.contains("has already been treated with") && normalized.contains("compost")
			|| normalized.contains("has already been fertilised with") && normalized.contains("compost");
	}

	static boolean isWateredMessage(String message)
	{
		String normalized = message == null ? "" : message.toLowerCase();
		return normalized.contains("has been watered") || normalized.contains("soil is watered");
	}

	static boolean isDeadObjectState(String name, String[] actions)
	{
		String normalized = name == null ? "" : name.toLowerCase();
		return normalized.contains("dead") || containsAction(actions, "Clear")
			&& !normalized.contains("stump");
	}

	static boolean isDiseasedObjectState(String name, String[] actions)
	{
		String normalized = name == null ? "" : name.toLowerCase();
		return normalized.contains("diseased") || containsAction(actions, "Cure");
	}

	private static boolean containsAction(String[] actions, String expected)
	{
		if (actions == null)
		{
			return false;
		}
		for (String action : actions)
		{
			if (expected.equalsIgnoreCase(action))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isWateredObjectName(String name)
	{
		String normalized = name == null ? "" : name.toLowerCase();
		return normalized.contains("watered");
	}

	private static boolean isCompostItem(int itemId)
	{
		return itemId == ItemID.BUCKET_COMPOST || itemId == ItemID.BUCKET_SUPERCOMPOST
			|| itemId == ItemID.BUCKET_ULTRACOMPOST || itemId == ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED;
	}

	private static boolean isWateringCan(int itemId)
	{
		return itemId == ItemID.WATERING_CAN_1 || itemId == ItemID.WATERING_CAN_2
			|| itemId == ItemID.WATERING_CAN_3 || itemId == ItemID.WATERING_CAN_4
			|| itemId == ItemID.WATERING_CAN_5 || itemId == ItemID.WATERING_CAN_6
			|| itemId == ItemID.WATERING_CAN_7 || itemId == ItemID.WATERING_CAN_8
			|| itemId == ItemID.ZEAH_WATERINGCAN;
	}

	private static String timerKey(WorldPoint location, PatchType patchType)
	{
		return location.getX() + ":" + location.getY() + ":" + location.getPlane() + ":" + patchType.name();
	}

	private PatchTimer findNearbyTimer(WorldPoint location, PatchType patchType, int maximumDistance)
	{
		PatchTimer nearest = null;
		int nearestDistance = maximumDistance + 1;
		for (PatchTimer timer : timers.values())
		{
			WorldPoint point = timer.getPatchLocation();
			int distance = Math.max(Math.abs(point.getX() - location.getX()),
				Math.abs(point.getY() - location.getY()));
			if (timer.getPatchType() == patchType && point.getPlane() == location.getPlane()
				&& distance < nearestDistance)
			{
				nearest = timer;
				nearestDistance = distance;
			}
		}
		return nearest;
	}

	private PatchTimer findNearestTimer(WorldPoint location, int maximumDistance)
	{
		PatchTimer nearest = null;
		int nearestDistance = maximumDistance + 1;
		for (PatchTimer timer : timers.values())
		{
			WorldPoint point = timer.getPatchLocation();
			if (point.getPlane() != location.getPlane())
			{
				continue;
			}
			int distance = Math.max(Math.abs(point.getX() - location.getX()),
				Math.abs(point.getY() - location.getY()));
			if (distance < nearestDistance)
			{
				nearest = timer;
				nearestDistance = distance;
			}
		}
		return nearest;
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
			|| normalized.equals("clear");
	}

	private static final class CareState
	{
		private boolean compostApplied;
		private boolean watered;
	}

	private static final class PendingCareAction
	{
		private boolean compostApplied;
		private boolean watered;
		private int objectStateId;
		private long startedAtMillis;
	}

	private static final class Inspection
	{
		private final WorldPoint location;
		private final PatchType patchType;
		private final long startedAtMillis;
		private Crop crop;
		private Integer currentStage;
		private Integer totalStages;
		private boolean completed;
		private boolean compostApplied;
		private boolean watered;

		private Inspection(WorldPoint location, PatchType patchType, Crop crop, long startedAtMillis)
		{
			this.location = location;
			this.patchType = patchType;
			this.crop = crop;
			this.startedAtMillis = startedAtMillis;
		}
	}
}
