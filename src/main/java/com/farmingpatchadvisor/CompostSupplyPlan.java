package com.farmingpatchadvisor;

final class CompostSupplyPlan
{
	private final int ingredientQuantity;
	private final int volcanicAshQuantity;
	private final int compostPotionDoses;
	private final int bucketQuantity;

	CompostSupplyPlan(int ingredientQuantity, int volcanicAshQuantity,
		int compostPotionDoses, int bucketQuantity)
	{
		this.ingredientQuantity = ingredientQuantity;
		this.volcanicAshQuantity = volcanicAshQuantity;
		this.compostPotionDoses = compostPotionDoses;
		this.bucketQuantity = bucketQuantity;
	}

	int getIngredientQuantity() { return ingredientQuantity; }
	int getVolcanicAshQuantity() { return volcanicAshQuantity; }
	int getCompostPotionDoses() { return compostPotionDoses; }
	int getBucketQuantity() { return bucketQuantity; }
}
