package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;

@Singleton
final class FarmRouteManager
{
	private static final String GROUP = "farming-patch-advisor";
	private final ConfigManager configManager;
	private final FarmingPatchAdvisorConfig config;
	private final FarmRunFilterState filter;

	@Inject
	FarmRouteManager(ConfigManager configManager, FarmingPatchAdvisorConfig config, FarmRunFilterState filter)
	{
		this.configManager = configManager;
		this.config = config;
		this.filter = filter;
	}

	static String key(FarmRunPatch patch)
	{
		return patch.getLocation() + "~" + patch.getPatchType().name() + "~" + patch.getName();
	}

	List<FarmRunPatch> activePatches()
	{
		List<FarmRunPatch> patches = new ArrayList<>(FarmRunCatalog.patches(config));
		Set<ChecklistPatch> selected = ChecklistPatch.selected(config);
		patches.removeIf(p -> !ChecklistPatch.includes(selected, p.getPatchType()) || !filter.includes(p.getPatchType()));
		return ordered(patches, filter.getSelected());
	}

	List<FarmRunPatch> ordered(List<FarmRunPatch> patches, FarmRunFilter run)
	{
		String stored = read("routeOrder_" + run.name());
		return order(patches, stored == null ? Collections.emptyList() : Arrays.asList(stored.split(";")));
	}

	static List<FarmRunPatch> order(List<FarmRunPatch> patches, List<String> keys)
	{
		Map<String, Integer> ranks = new LinkedHashMap<>();
		for (String key : keys) { ranks.putIfAbsent(key, ranks.size()); }
		List<FarmRunPatch> result = new ArrayList<>(patches);
		result.sort(Comparator.comparing(p -> p.getFarmRunType().ordinal()));
		result.sort(Comparator.comparingInt(p -> ranks.getOrDefault(key(p), Integer.MAX_VALUE)));
		return result;
	}

	boolean isCustom(FarmRunFilter run) { return read("routeOrder_" + run.name()) != null; }
	String profile() { return configManager.getRSProfileKey(); }

	RouteTeleport teleport(FarmRunFilter run, FarmRunPatch patch)
	{
		String value = read("routeTeleport_" + run.name() + "_" + key(patch));
		try { return value == null ? RouteTeleport.NONE : RouteTeleport.valueOf(value); }
		catch (IllegalArgumentException ex) { return RouteTeleport.NONE; }
	}

	boolean save(String profile, FarmRunFilter run, List<FarmRunPatch> patches,
		Map<String, RouteTeleport> choices, boolean custom)
	{
		if (profile == null || !Objects.equals(profile, profile())) { return false; }
		String orderKey = "routeOrder_" + run.name();
		if (custom)
		{
			// Preserve disabled stops so reenabling an area does not lose its position.
			List<String> keys = new ArrayList<>();
			for (FarmRunPatch p : patches) { keys.add(key(p)); }
			String previous = read(orderKey);
			keys = mergeOrder(keys, previous == null ? Collections.emptyList() : Arrays.asList(previous.split(";")));
			configManager.setRSProfileConfiguration(GROUP, orderKey, String.join(";", keys));
		}
		else { configManager.unsetRSProfileConfiguration(GROUP, orderKey); }
		for (Map.Entry<String, RouteTeleport> choice : choices.entrySet())
		{
			configManager.setRSProfileConfiguration(GROUP,
				"routeTeleport_" + run.name() + "_" + choice.getKey(), choice.getValue().name());
		}
		return true;
	}

	Map<RouteTeleport, Integer> requirements()
	{
		List<FarmRunPatch> active = activePatches();
		Map<String, RouteTeleport> choices = new LinkedHashMap<>();
		for (FarmRunPatch patch : active) { choices.put(key(patch), teleport(filter.getSelected(), patch)); }
		return requirements(active, choices);
	}

	static List<String> mergeOrder(List<String> visible, List<String> previous)
	{
		List<String> result = new ArrayList<>();
		int next = 0;
		for (String key : previous)
		{
			if (visible.contains(key))
			{
				if (next < visible.size()) { result.add(visible.get(next++)); }
			}
			else if (!result.contains(key)) { result.add(key); }
		}
		while (next < visible.size()) { result.add(visible.get(next++)); }
		return result;
	}

	static Map<RouteTeleport, Integer> requirements(List<FarmRunPatch> patches, Map<String, RouteTeleport> choices)
	{
		Map<RouteTeleport, Integer> requirements = new LinkedHashMap<>();
		// Multiple patches in one stop share travel when adjacent in the route.
		String previousStop = null;
		for (FarmRunPatch patch : patches)
		{
			RouteTeleport teleport = choices.getOrDefault(key(patch), RouteTeleport.NONE);
			String stop = patch.getLocation() + "~" + teleport.name();
			if (teleport != RouteTeleport.NONE && !stop.equals(previousStop))
			{
				requirements.merge(teleport, 1, teleport.consumable ? Integer::sum : Math::max);
			}
			previousStop = stop;
		}
		return requirements;
	}

	private String read(String key)
	{
		return profile() == null ? null : configManager.getRSProfileConfiguration(GROUP, key);
	}
}
