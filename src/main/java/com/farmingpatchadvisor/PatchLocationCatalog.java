package com.farmingpatchadvisor;

import java.util.HashMap;
import java.util.Map;
import net.runelite.api.coords.WorldPoint;

final class PatchLocationCatalog
{
	private static final Map<Integer, String> NAMES = buildNames();

	private PatchLocationCatalog()
	{
	}

	static String name(WorldPoint point)
	{
		if (point.getRegionID() == 12083 && point.getY() < 3272)
		{
			return "Port Sarim";
		}
		return NAMES.getOrDefault(point.getRegionID(), "Region " + point.getRegionID());
	}

	static int routeRank(PatchTimer timer)
	{
		String location = name(timer.getPatchLocation());
		switch (timer.getPatchType())
		{
			case ALLOTMENT:
			case FLOWER:
			case HERB:
				return index(location, "Falador", "Morytania", "Catherby", "Ardougne", "Kourend",
					"Troll Stronghold", "Harmony Island", "Weiss", "Farming Guild", "Civitas illa Fortis");
			case TREE:
				return 1000 + index(location, "Lumbridge", "Varrock", "Falador", "Taverley", "Gnome Stronghold",
					"Farming Guild", "Auburnvale");
			case FRUIT_TREE:
			case CALQUAT:
				return 2000 + index(location, "Gnome Stronghold", "Tree Gnome Village", "Catherby", "Farming Guild",
					"Lletya", "Brimhaven", "Tai Bwo Wannai", "Kastori", "Great Conch");
			case HARDWOOD_TREE:
				return 3000 + index(location, "Fossil Island", "Avium Savannah", "Anglers' Retreat");
			case HOPS:
				return 4000 + index(location, "Yanille", "Seers' Village", "Lumbridge", "Entrana", "Aldarin");
			case BUSH:
				return 5000 + index(location, "Champions' Guild", "Rimmington", "Ardougne", "Etceteria", "Farming Guild");
			case CACTUS:
				return 6000 + index(location, "Al Kharid", "Farming Guild");
			case SEAWEED:
			case MUSHROOM:
			case BELLADONNA:
			case GRAPEVINE:
			case CELASTRUS:
			case REDWOOD:
			case HESPORI:
			case SPIRIT_TREE:
			case CRYSTAL_TREE:
			case CORAL:
				return 7000 + index(location, "Farming Guild", "Seaweed", "Fossil Island", "Morytania",
					"Draynor Manor", "Kourend", "Prifddinas", "Port Sarim", "Etceteria", "Brimhaven", "Great Conch");
			default:
				return 9000 + (location.hashCode() & 0x7fffffff);
		}
	}

	private static int index(String value, String... ordered)
	{
		for (int i = 0; i < ordered.length; i++)
		{
			if (ordered[i].equals(value))
			{
				return i;
			}
		}
		return 1000;
	}

	private static Map<Integer, String> buildNames()
	{
		Map<Integer, String> names = new HashMap<>();
		add(names, "Al Kharid", 13106, 13362, 13105);
		add(names, "Aldarin", 5421, 5165, 5166, 5422, 5677, 5678);
		add(names, "Anglers' Retreat", 9770);
		add(names, "Ardougne", 10290, 10546, 10548);
		add(names, "Auburnvale", 5427, 5428, 5684);
		add(names, "Avium Savannah", 6702, 6446);
		add(names, "Brimhaven", 11058, 11057);
		add(names, "Catherby", 11062, 11061, 11318, 11317);
		add(names, "Civitas illa Fortis", 6192, 6447, 6448, 6449, 6191, 6193);
		add(names, "Champions' Guild", 12596);
		add(names, "Draynor Manor", 12340);
		add(names, "Entrana", 11060, 11316);
		add(names, "Etceteria", 10300);
		add(names, "Falador", 11828, 12084, 12083);
		add(names, "Fossil Island", 14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163);
		add(names, "Seaweed", 15008);
		add(names, "Gnome Stronghold", 9781, 9782, 9526, 9525);
		add(names, "Great Conch", 12581, 12325, 12326, 12327, 12580, 12582, 12583, 12836, 12837, 12838, 12839, 13092, 13093, 13194);
		add(names, "Harmony Island", 15148);
		add(names, "Kastori", 5423, 5167, 5424);
		add(names, "Kourend", 6967, 6711, 7223);
		add(names, "Lletya", 9265, 11103);
		add(names, "Lumbridge", 12851, 12594, 12850);
		add(names, "Morytania", 13622, 13878, 14391, 14390);
		add(names, "Port Sarim", 12082);
		add(names, "Rimmington", 11570, 11826);
		add(names, "Seers' Village", 10551, 10550);
		add(names, "Tai Bwo Wannai", 11056);
		add(names, "Taverley", 11573, 11829);
		add(names, "Tree Gnome Village", 9777, 10033);
		add(names, "Troll Stronghold", 11321);
		add(names, "Varrock", 12854, 12853);
		add(names, "Yanille", 10288);
		add(names, "Weiss", 11325);
		add(names, "Farming Guild", 5021, 4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667);
		add(names, "Prifddinas", 13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250);
		return names;
	}

	private static void add(Map<Integer, String> names, String name, int... regionIds)
	{
		for (int regionId : regionIds)
		{
			names.put(regionId, name);
		}
	}
}
