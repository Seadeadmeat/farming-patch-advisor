package com.farmingpatchadvisor;

final class Crop
{
	private final PatchType patchType;
	private final String cropName;
	private final String itemName;
	private final int level;
	private final int itemId;
	private final int quantity;

	Crop(PatchType patchType, String cropName, String itemName, int level, int itemId, int quantity)
	{
		this.patchType = patchType;
		this.cropName = cropName;
		this.itemName = itemName;
		this.level = level;
		this.itemId = itemId;
		this.quantity = quantity;
	}

	PatchType getPatchType()
	{
		return patchType;
	}

	String getName()
	{
		return cropName;
	}

	String getItemName()
	{
		return itemName;
	}

	int getLevel()
	{
		return level;
	}

	int getItemId()
	{
		return itemId;
	}

	int getQuantity()
	{
		return quantity;
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof Crop && ((Crop) other).itemId == itemId;
	}

	@Override
	public int hashCode()
	{
		return itemId;
	}
}
