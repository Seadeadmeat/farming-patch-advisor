package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

final class FarmRunCatalog
{
	private static final List<FarmRunPatch> PATCHES = build();

	private FarmRunCatalog()
	{
	}

	static List<FarmRunPatch> patches()
	{
		return PATCHES;
	}

	static List<FarmRunPatch> patches(FarmingPatchAdvisorConfig config)
	{
		List<FarmRunPatch> enabled = new ArrayList<>();
		for (FarmRunPatch patch : PATCHES)
		{
			if (PatchLocationSelection.isEnabled(config, patch.getLocation()))
			{
				enabled.add(patch);
			}
		}
		return enabled;
	}

	static boolean hasPatch(String location, PatchType patchType)
	{
		for (FarmRunPatch patch : PATCHES)
		{
			if (patch.getLocation().equals(location) && patch.getPatchType() == patchType)
			{
				return true;
			}
		}
		return false;
	}

	private static List<FarmRunPatch> build()
	{
		List<FarmRunPatch> patches = new ArrayList<>();

		// Frequent allotment, flower, and herb run.
		farm(patches, "Falador", true, true, true);
		farm(patches, "Morytania", true, true, true);
		farm(patches, "Catherby", true, true, true);
		farm(patches, "Ardougne", true, true, true);
		farm(patches, "Kourend", true, true, true);
		add(patches, "Troll Stronghold", "", PatchType.HERB);
		add(patches, "Harmony Island", "", PatchType.ALLOTMENT);
		add(patches, "Harmony Island", "", PatchType.HERB);
		add(patches, "Weiss", "", PatchType.HERB);
		farm(patches, "Farming Guild", true, true, true);
		farm(patches, "Civitas illa Fortis", true, true, true);
		add(patches, "Prifddinas", "North", PatchType.ALLOTMENT);
		add(patches, "Prifddinas", "South", PatchType.ALLOTMENT);
		add(patches, "Prifddinas", "", PatchType.FLOWER);

		// Regular tree run.
		for (String location : new String[]{"Lumbridge", "Varrock", "Gnome Stronghold", "Falador", "Taverley", "Farming Guild", "Auburnvale"})
		{
			add(patches, location, "", PatchType.TREE);
		}

		// Fruit tree and calquat run.
		add(patches, "Gnome Stronghold", "", PatchType.FRUIT_TREE);
		add(patches, "Tree Gnome Village", "", PatchType.FRUIT_TREE);
		add(patches, "Catherby", "", PatchType.FRUIT_TREE);
		add(patches, "Farming Guild", "", PatchType.FRUIT_TREE);
		add(patches, "Lletya", "", PatchType.FRUIT_TREE);
		add(patches, "Brimhaven", "", PatchType.FRUIT_TREE);
		add(patches, "Tai Bwo Wannai", "", PatchType.CALQUAT);
		add(patches, "Kastori", "Calquat", PatchType.CALQUAT);
		add(patches, "Kastori", "Fruit tree", PatchType.FRUIT_TREE);
		add(patches, "Great Conch", "", PatchType.CALQUAT);

		// Hardwood run.
		add(patches, "Fossil Island", "East", PatchType.HARDWOOD_TREE);
		add(patches, "Fossil Island", "Middle", PatchType.HARDWOOD_TREE);
		add(patches, "Fossil Island", "West", PatchType.HARDWOOD_TREE);
		add(patches, "Avium Savannah", "", PatchType.HARDWOOD_TREE);
		add(patches, "Anglers' Retreat", "", PatchType.HARDWOOD_TREE);

		// Hops run.
		for (String location : new String[]{"Yanille", "Seers' Village", "Lumbridge", "Entrana", "Aldarin"})
		{
			add(patches, location, "", PatchType.HOPS);
		}

		// Bush run.
		for (String location : new String[]{"Champions' Guild", "Rimmington", "Ardougne", "Etceteria", "Farming Guild"})
		{
			add(patches, location, "", PatchType.BUSH);
		}

		add(patches, "Al Kharid", "", PatchType.CACTUS);
		add(patches, "Farming Guild", "", PatchType.CACTUS);

		// Specialty patches, grouped to minimize repeat travel where possible.
		add(patches, "Farming Guild", "", PatchType.HESPORI);
		add(patches, "Farming Guild", "", PatchType.ANIMA);
		add(patches, "Farming Guild", "", PatchType.CELASTRUS);
		add(patches, "Farming Guild", "", PatchType.REDWOOD);
		add(patches, "Farming Guild", "", PatchType.SPIRIT_TREE);
		add(patches, "Seaweed", "North", PatchType.SEAWEED);
		add(patches, "Seaweed", "South", PatchType.SEAWEED);
		add(patches, "Morytania", "", PatchType.MUSHROOM);
		add(patches, "Draynor Manor", "", PatchType.BELLADONNA);
		add(patches, "Auburnvale", "", PatchType.BELLADONNA);
		for (int i = 1; i <= 12; i++)
		{
			add(patches, "Kourend", "Grapevine " + i, PatchType.GRAPEVINE);
		}
		add(patches, "Kourend", "", PatchType.SPIRIT_TREE);
		add(patches, "Prifddinas", "", PatchType.CRYSTAL_TREE);
		add(patches, "Port Sarim", "", PatchType.SPIRIT_TREE);
		add(patches, "Etceteria", "", PatchType.SPIRIT_TREE);
		add(patches, "Brimhaven", "", PatchType.SPIRIT_TREE);
		add(patches, "Great Conch", "East", PatchType.CORAL);
		add(patches, "Great Conch", "West", PatchType.CORAL);
		add(patches, "Kastori", "", PatchType.FLOWER);

		return Collections.unmodifiableList(groupRepeatedPatches(patches));
	}

	private static List<FarmRunPatch> groupRepeatedPatches(List<FarmRunPatch> patches)
	{
		Map<String, List<FarmRunPatch>> groups = new LinkedHashMap<>();
		for (FarmRunPatch patch : patches)
		{
			String key = patch.getLocation() + ":" + patch.getPatchType();
			groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(patch);
		}

		List<FarmRunPatch> grouped = new ArrayList<>();
		for (List<FarmRunPatch> group : groups.values())
		{
			FarmRunPatch first = group.get(0);
			String name = group.size() > 1 ? pluralName(first.getPatchType()) : first.getName();
			grouped.add(new FarmRunPatch(first.getLocation(), name, first.getPatchType(), group.size()));
		}
		return grouped;
	}

	private static String pluralName(PatchType patchType)
	{
		switch (patchType)
		{
			case ALLOTMENT: return "Allotments";
			case GRAPEVINE: return "Grapevines";
			case HARDWOOD_TREE: return "Hardwood trees";
			case SEAWEED: return "Seaweed patches";
			case CORAL: return "Coral patches";
			default: return patchType.getDisplayName() + " patches";
		}
	}

	private static void farm(List<FarmRunPatch> patches, String location, boolean allotments, boolean flower, boolean herb)
	{
		if (allotments)
		{
			add(patches, location, "Allotment 1", PatchType.ALLOTMENT);
			add(patches, location, "Allotment 2", PatchType.ALLOTMENT);
		}
		if (flower)
		{
			add(patches, location, "", PatchType.FLOWER);
		}
		if (herb)
		{
			add(patches, location, "", PatchType.HERB);
		}
	}

	private static void add(List<FarmRunPatch> patches, String location, String name, PatchType patchType)
	{
		patches.add(new FarmRunPatch(location, name, patchType));
	}
}
