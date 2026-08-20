package com.farmingpatchadvisor;

enum PatchType
{
	ALLOTMENT("Allotment", true),
	FLOWER("Flower", true),
	HERB("Herb", true),
	HOPS("Hops", true),
	BUSH("Bush", true),
	TREE("Tree", false),
	FRUIT_TREE("Fruit tree", false),
	HARDWOOD_TREE("Hardwood tree", false),
	CACTUS("Cactus", true),
	MUSHROOM("Mushroom", true),
	BELLADONNA("Belladonna", true),
	CALQUAT("Calquat", false),
	SPIRIT_TREE("Spirit tree", false),
	SEAWEED("Seaweed", true),
	GRAPEVINE("Grapevine", true),
	CELASTRUS("Celastrus", false),
	REDWOOD("Redwood", false),
	HESPORI("Hespori", true),
	CRYSTAL_TREE("Crystal tree", false),
	CORAL("Coral", true);

	private final String displayName;
	private final boolean usesDibber;

	PatchType(String displayName, boolean usesDibber)
	{
		this.displayName = displayName;
		this.usesDibber = usesDibber;
	}

	String getDisplayName()
	{
		return displayName;
	}

	boolean usesDibber()
	{
		return usesDibber;
	}

	boolean canBeWatered()
	{
		return this == ALLOTMENT || this == FLOWER || this == HOPS;
	}

	boolean usesCompost()
	{
		return this != CRYSTAL_TREE;
	}
}
