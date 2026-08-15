package com.farmingpatchadvisor;

final class FarmRunPatch
{
	private final String location;
	private final String name;
	private final PatchType patchType;
	private final int patchCount;

	FarmRunPatch(String location, String name, PatchType patchType)
	{
		this(location, name, patchType, 1);
	}

	FarmRunPatch(String location, String name, PatchType patchType, int patchCount)
	{
		this.location = location;
		this.name = name;
		this.patchType = patchType;
		this.patchCount = patchCount;
	}

	String getLocation()
	{
		return location;
	}

	String getName()
	{
		return name;
	}

	PatchType getPatchType()
	{
		return patchType;
	}

	FarmRunType getFarmRunType()
	{
		return FarmRunType.forPatchType(patchType);
	}

	int getPatchCount()
	{
		return patchCount;
	}

	String getDisplayName()
	{
		String display = name.isEmpty() ? location : location + " - " + name;
		return patchCount > 1 ? display + " (" + patchCount + ")" : display;
	}
}
