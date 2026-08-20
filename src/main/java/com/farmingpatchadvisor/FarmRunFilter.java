package com.farmingpatchadvisor;

import java.util.EnumSet;
import java.util.Set;

enum FarmRunFilter
{
	ALL("All", EnumSet.allOf(FarmRunType.class)),
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

	FarmRunFilter(FarmRunType runType)
	{
		this(runType.getDisplayName(), EnumSet.of(runType));
	}

	FarmRunFilter(String displayName, Set<FarmRunType> runTypes)
	{
		this.displayName = displayName;
		this.runTypes = runTypes;
	}

	boolean includes(FarmRunType runType)
	{
		return runTypes.contains(runType);
	}

	boolean isAvailable(Set<FarmRunType> availableRunTypes)
	{
		return this == ALL || availableRunTypes.containsAll(runTypes);
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
