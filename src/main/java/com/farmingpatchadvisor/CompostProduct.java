package com.farmingpatchadvisor;

enum CompostProduct
{
	EMPTY("Empty"),
	COMPOST("Compost"),
	SUPERCOMPOST("Supercompost"),
	ULTRACOMPOST("Ultracompost"),
	ROTTEN_TOMATO("Rotten tomato"),
	UNKNOWN("Unknown");

	private final String displayName;

	CompostProduct(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
