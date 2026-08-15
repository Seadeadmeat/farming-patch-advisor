package com.farmingpatchadvisor;

public enum SeedSelectionMode
{
	HIGHEST_AVAILABLE("Highest available seeds"),
	HIGHEST_LEVEL("Highest-level seeds");

	private final String displayName;

	SeedSelectionMode(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
