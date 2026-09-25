package com.farmingpatchadvisor;

import net.runelite.api.gameval.ItemID;

public enum CompostTarget
{
	COMPOST("Compost", CompostProduct.COMPOST, "Weeds", ItemID.WEEDS),
	SUPERCOMPOST("Supercompost", CompostProduct.SUPERCOMPOST, "Watermelon", ItemID.WATERMELON),
	ULTRACOMPOST("Ultracompost", CompostProduct.ULTRACOMPOST, "Watermelon", ItemID.WATERMELON);

	private final String displayName;
	private final CompostProduct product;
	private final String ingredientName;
	private final int ingredientItemId;

	CompostTarget(String displayName, CompostProduct product, String ingredientName, int ingredientItemId)
	{
		this.displayName = displayName;
		this.product = product;
		this.ingredientName = ingredientName;
		this.ingredientItemId = ingredientItemId;
	}

	CompostProduct getProduct()
	{
		return product;
	}

	String getIngredientName()
	{
		return ingredientName;
	}

	int getIngredientItemId()
	{
		return ingredientItemId;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
