package com.farmingpatchadvisor;

final class FarmingContract
{
	private final String name;
	private final Crop crop;

	FarmingContract(String name, Crop crop)
	{
		this.name = name;
		this.crop = crop;
	}

	String getName()
	{
		return name;
	}

	Crop getCrop()
	{
		return crop;
	}
}
