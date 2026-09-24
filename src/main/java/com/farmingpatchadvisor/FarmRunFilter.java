package com.farmingpatchadvisor;

import java.util.EnumSet;
import java.util.Set;

enum FarmRunFilter
{
	ALL("All", EnumSet.allOf(FarmRunType.class), false),
	COMPOST("Compost Run", EnumSet.noneOf(FarmRunType.class), true),
	ALLOTMENT_FLOWER(FarmRunType.ALLOTMENT_FLOWER),
	HERB(FarmRunType.HERB),
	TREE(FarmRunType.TREE),
	FRUIT_TREE_CALQUAT(FarmRunType.FRUIT_TREE_CALQUAT),
	TREE_AND_FRUIT_TREE("Tree + Fruit Tree", EnumSet.of(FarmRunType.TREE, FarmRunType.FRUIT_TREE_CALQUAT)),
	HARDWOOD_TREE(FarmRunType.HARDWOOD_TREE),
	HOPS(FarmRunType.HOPS),
	BUSH(FarmRunType.BUSH),
	CACTUS(FarmRunType.CACTUS),
	SPECIALTY(FarmRunType.SPECIALTY);

	private final String displayName;
	private final Set<FarmRunType> runTypes;
	private final boolean compostOnly;

	FarmRunFilter(FarmRunType runType)
	{
		this(runType.getDisplayName(), EnumSet.of(runType), false);
	}

	FarmRunFilter(String displayName, Set<FarmRunType> runTypes)
	{
		this(displayName, runTypes, false);
	}

	FarmRunFilter(String displayName, Set<FarmRunType> runTypes, boolean compostOnly)
	{
		this.displayName = displayName;
		this.runTypes = runTypes;
		this.compostOnly = compostOnly;
	}

	boolean includes(FarmRunType runType)
	{
		return runTypes.contains(runType);
	}

	boolean isAvailable(Set<FarmRunType> availableRunTypes)
	{
		return isAvailable(availableRunTypes, false);
	}

	boolean isAvailable(Set<FarmRunType> availableRunTypes, boolean compostAvailable)
	{
		if (this == ALL)
		{
			return true;
		}
		return compostOnly ? compostAvailable : availableRunTypes.containsAll(runTypes);
	}

	boolean isCompostOnly()
	{
		return compostOnly;
	}

	boolean includesCompost(boolean includeWithAll)
	{
		return compostOnly || this == ALL && includeWithAll;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
