package com.farmingpatchadvisor;

import java.util.EnumSet;
import net.runelite.api.gameval.ItemID;

/** Explicit player choices: this does not infer unlocks or remaining daily charges. */
enum RouteTeleport
{
	NONE("None / no item needed", false),
	ECTOPHIAL("Ectophial", false, ItemID.ECTOPHIAL),
	XERIC("Xeric's talisman", false, ItemID.XERIC_TALISMAN),
	FARM_CAPE("Farming cape", false, ItemID.SKILLCAPE_FARMING),
	FARM_CAPE_TRIMMED("Farming cape(t)", false, ItemID.SKILLCAPE_FARMING_TRIMMED),
	ROYAL_POD("Royal seed pod", false, ItemID.MM2_ROYAL_SEED_POD),
	SEED_POD("Grand seed pod", true, ItemID.ALUFT_SEED_POD),
	QUETZAL_BASIC("Basic quetzal whistle", false, ItemID.HG_QUETZALWHISTLE_BASIC),
	QUETZAL_ENHANCED("Enhanced quetzal whistle", false, ItemID.HG_QUETZALWHISTLE_ENHANCED),
	QUETZAL_PERFECTED("Perfected quetzal whistle", false, ItemID.HG_QUETZALWHISTLE_PERFECTED),
	EXPLORER_2("Explorer's ring 2", false, ItemID.LUMBRIDGE_RING_MEDIUM),
	EXPLORER_3("Explorer's ring 3", false, ItemID.LUMBRIDGE_RING_HARD),
	EXPLORER_4("Explorer's ring 4", false, ItemID.LUMBRIDGE_RING_ELITE),
	ARDY_2("Ardougne cloak 2", false, ItemID.ARDY_CAPE_MEDIUM),
	ARDY_3("Ardougne cloak 3", false, ItemID.ARDY_CAPE_HARD),
	ARDY_4("Ardougne cloak 4", false, ItemID.ARDY_CAPE_ELITE),
	CAMELOT("Camelot teleport", true, ItemID.POH_TABLET_CAMELOTTELEPORT),
	CATHERBY("Catherby teleport", true, ItemID.LUNAR_TABLET_CATHERBY_TELEPORT),
	VARROCK("Varrock teleport", true, ItemID.POH_TABLET_VARROCKTELEPORT),
	LUMBRIDGE("Lumbridge teleport", true, ItemID.POH_TABLET_LUMBRIDGETELEPORT),
	FALADOR("Falador teleport", true, ItemID.POH_TABLET_FALADORTELEPORT),
	ARDOUGNE("Ardougne teleport", true, ItemID.POH_TABLET_ARDOUGNETELEPORT),
	HOUSE("Teleport to house", true, ItemID.POH_TABLET_TELEPORTTOHOUSE),
	HARMONY("Harmony island teleport", true, ItemID.TELETAB_HARMONY),
	TAVERLEY("Taverley teleport", true, ItemID.NZONE_TELETAB_TAVERLEY),
	BRIMHAVEN("Brimhaven teleport", true, ItemID.NZONE_TELETAB_BRIMHAVEN),
	YANILLE("Yanille teleport", true, ItemID.NZONE_TELETAB_YANILLE),
	TROLLHEIM("Trollheim teleport", true, ItemID.NZONE_TELETAB_TROLLHEIM),
	WEISS("Icy basalt", true, ItemID.WEISS_TELEPORT_BASALT),
	STRONGHOLD("Stony basalt", true, ItemID.STRONGHOLD_TELEPORT_BASALT),
	FORTIS("Civitas illa fortis teleport", true, ItemID.POH_TABLET_FORTISTELEPORT),
	SKILLS("Skills necklace (charged)", false, ItemID.JEWL_NECKLACE_OF_SKILLS_1,
		ItemID.JEWL_NECKLACE_OF_SKILLS_2, ItemID.JEWL_NECKLACE_OF_SKILLS_3,
		ItemID.JEWL_NECKLACE_OF_SKILLS_4, ItemID.JEWL_NECKLACE_OF_SKILLS_5, ItemID.JEWL_NECKLACE_OF_SKILLS_6),
	DIGSITE("Digsite pendant (charged)", false, ItemID.NECKLACE_OF_DIGSITE_1,
		ItemID.NECKLACE_OF_DIGSITE_2, ItemID.NECKLACE_OF_DIGSITE_3,
		ItemID.NECKLACE_OF_DIGSITE_4, ItemID.NECKLACE_OF_DIGSITE_5),
	CRYSTAL("Teleport crystal (charged)", false, ItemID.MOURNING_TELEPORT_CRYSTAL_1,
		ItemID.MOURNING_TELEPORT_CRYSTAL_2, ItemID.MOURNING_TELEPORT_CRYSTAL_3,
		ItemID.MOURNING_TELEPORT_CRYSTAL_4, ItemID.MOURNING_TELEPORT_CRYSTAL_5);

	private final String label;
	final boolean consumable;
	private final int[] itemIds;

	RouteTeleport(String label, boolean consumable, int... itemIds)
	{
		this.label = label;
		this.consumable = consumable;
		this.itemIds = itemIds;
	}

	int[] itemIds() { return itemIds.clone(); }

	boolean isViableFor(FarmRunPatch patch)
	{
		return viableFor(patch).contains(this);
	}

	static RouteTeleport[] viableValues(FarmRunPatch patch)
	{
		EnumSet<RouteTeleport> viable = viableFor(patch);
		return viable.toArray(new RouteTeleport[0]);
	}

	private static EnumSet<RouteTeleport> viableFor(FarmRunPatch patch)
	{
		EnumSet<RouteTeleport> viable = EnumSet.of(NONE);
		String location = patch == null ? "" : patch.getLocation();
		switch (location)
		{
			case "Falador": add(viable, EXPLORER_2, EXPLORER_3, EXPLORER_4, FALADOR, SKILLS); break;
			case "Morytania": add(viable, ECTOPHIAL); break;
			case "Catherby": add(viable, CATHERBY, CAMELOT); break;
			case "Ardougne": add(viable, ARDY_2, ARDY_3, ARDY_4, ARDOUGNE, SKILLS); break;
			case "Kourend": add(viable, XERIC, SKILLS, HOUSE); break;
			case "Troll Stronghold": add(viable, STRONGHOLD, TROLLHEIM, HOUSE); break;
			case "Harmony Island": add(viable, HARMONY, ECTOPHIAL); break;
			case "Weiss": add(viable, WEISS, HOUSE); break;
			case "Farming Guild": add(viable, FARM_CAPE, FARM_CAPE_TRIMMED, SKILLS); break;
			case "Civitas illa Fortis": add(viable, QUETZAL_BASIC, QUETZAL_ENHANCED, QUETZAL_PERFECTED, FORTIS); break;
			case "Prifddinas":
			case "Lletya": add(viable, CRYSTAL); break;
			case "Lumbridge": add(viable, LUMBRIDGE); break;
			case "Varrock":
			case "Champions' Guild": add(viable, VARROCK); break;
			case "Gnome Stronghold":
			case "Tree Gnome Village": add(viable, ROYAL_POD, SEED_POD); break;
			case "Taverley": add(viable, TAVERLEY, HOUSE); break;
			case "Auburnvale":
			case "Kastori":
			case "Avium Savannah": add(viable, QUETZAL_BASIC, QUETZAL_ENHANCED, QUETZAL_PERFECTED); break;
			case "Brimhaven":
			case "Tai Bwo Wannai": add(viable, BRIMHAVEN, HOUSE); break;
			case "Fossil Island":
			case "Seaweed": add(viable, DIGSITE); break;
			case "Yanille": add(viable, YANILLE, HOUSE); break;
			case "Seers' Village": add(viable, CAMELOT); break;
			case "Entrana": add(viable, FALADOR); break;
			case "Aldarin": add(viable, HOUSE, QUETZAL_BASIC, QUETZAL_ENHANCED, QUETZAL_PERFECTED); break;
			case "Rimmington": add(viable, HOUSE, FALADOR, SKILLS); break;
			case "Etceteria": add(viable, HOUSE); break;
			case "Al Kharid": add(viable, LUMBRIDGE); break;
			case "Draynor Manor": add(viable, LUMBRIDGE, EXPLORER_2, EXPLORER_3, EXPLORER_4); break;
			case "Port Sarim": add(viable, FALADOR, EXPLORER_2, EXPLORER_3, EXPLORER_4); break;
			default: break;
		}
		return viable;
	}

	private static void add(EnumSet<RouteTeleport> values, RouteTeleport... additions)
	{
		for (RouteTeleport addition : additions) { values.add(addition); }
	}

	static boolean isTeleportItem(int itemId)
	{
		for (RouteTeleport teleport : values())
		{
			for (int id : teleport.itemIds) { if (id == itemId) { return true; } }
		}
		return false;
	}

	@Override public String toString() { return label; }
}
