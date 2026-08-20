package com.farmingpatchadvisor;

enum FarmRunType
{
	ALLOTMENT_FLOWER("Allotment & Flower Run"),
	HERB("Herb Run"),
	TREE("Tree Run"),
	FRUIT_TREE_CALQUAT("Fruit Tree & Calquat Run"),
	HARDWOOD_TREE("Hardwood Tree Run"),
	HOPS("Hops Run"),
	BUSH("Bush Run"),
	CACTUS("Cactus Run"),
	SPECIALTY("Specialty Patch Run");

	private final String displayName;

	FarmRunType(String displayName)
	{
		this.displayName = displayName;
	}

	String getDisplayName()
	{
		return displayName;
	}

	static FarmRunType forPatchType(PatchType patchType)
	{
		switch (patchType)
		{
			case ALLOTMENT:
			case FLOWER:
				return ALLOTMENT_FLOWER;
			case HERB:
				return HERB;
			case TREE:
				return TREE;
			case FRUIT_TREE:
			case CALQUAT:
				return FRUIT_TREE_CALQUAT;
			case HARDWOOD_TREE:
				return HARDWOOD_TREE;
			case HOPS:
				return HOPS;
			case BUSH:
				return BUSH;
			case CACTUS:
				return CACTUS;
			case MUSHROOM:
			case BELLADONNA:
			case SPIRIT_TREE:
			case SEAWEED:
			case GRAPEVINE:
			case CELASTRUS:
			case REDWOOD:
			case HESPORI:
			case CRYSTAL_TREE:
			case CORAL:
				return SPECIALTY;
			default:
				throw new IllegalArgumentException("Unsupported patch type: " + patchType);
		}
	}
}
