package com.farmingpatchadvisor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.VarbitID;

enum CompostBinLocation
{
	FALADOR("Falador", false, VarbitID.FARMING_TRANSMIT_E, 12083),
	MORYTANIA("Morytania", false, VarbitID.FARMING_TRANSMIT_E, 14391, 14390),
	CATHERBY("Catherby", false, VarbitID.FARMING_TRANSMIT_E, 11062, 11061, 11318, 11317),
	ARDOUGNE("Ardougne", false, VarbitID.FARMING_TRANSMIT_E, 10548),
	KOUREND("Kourend", false, VarbitID.FARMING_TRANSMIT_E, 6967, 6711),
	FARMING_GUILD("Farming Guild", true, VarbitID.FARMING_TRANSMIT_N,
		4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667),
	CIVITAS("Civitas illa Fortis", false, VarbitID.FARMING_TRANSMIT_E,
		6192, 6447, 6448, 6449, 6191, 6193),
	PRIFDDINAS("Prifddinas", false, VarbitID.FARMING_TRANSMIT_D,
		13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250);

	private static final List<CompostBinLocation> ROUTE = Collections.unmodifiableList(Arrays.asList(values()));
	private final String displayName;
	private final boolean big;
	private final int varbitId;
	private final int[] regionIds;

	CompostBinLocation(String displayName, boolean big, int varbitId, int... regionIds)
	{
		this.displayName = displayName;
		this.big = big;
		this.varbitId = varbitId;
		this.regionIds = regionIds;
	}

	String getDisplayName() { return displayName; }
	boolean isBig() { return big; }
	int getVarbitId() { return varbitId; }
	int getCapacity() { return big ? 30 : 15; }
	String getBinName() { return big ? "Big compost bin" : "Compost bin"; }

	boolean contains(WorldPoint point)
	{
		if (point == null)
		{
			return false;
		}
		for (int regionId : regionIds)
		{
			if (point.getRegionID() == regionId)
			{
				return this != FALADOR || point.getY() >= 3272;
			}
		}
		return false;
	}

	boolean isEnabled(FarmingPatchAdvisorConfig config)
	{
		if (!PatchLocationSelection.isEnabled(config, displayName))
		{
			return false;
		}
		switch (this)
		{
			case FALADOR: return config.compostFalador();
			case MORYTANIA: return config.compostMorytania();
			case CATHERBY: return config.compostCatherby();
			case ARDOUGNE: return config.compostArdougne();
			case KOUREND: return config.compostKourend();
			case FARMING_GUILD: return config.compostFarmingGuild();
			case CIVITAS: return config.compostCivitas();
			case PRIFDDINAS: return config.compostPrifddinas();
			default: return false;
		}
	}

	static CompostBinLocation at(WorldPoint point)
	{
		for (CompostBinLocation location : values())
		{
			if (location.contains(point))
			{
				return location;
			}
		}
		return null;
	}

	static List<CompostBinLocation> route()
	{
		return ROUTE;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
