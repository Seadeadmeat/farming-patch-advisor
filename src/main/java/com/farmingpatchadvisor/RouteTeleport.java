package com.farmingpatchadvisor;

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
