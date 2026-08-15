package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CropCatalogTest
{
	@Test
	public void usesActualPerPatchQuantitiesForGroupedAndNewCrops()
	{
		assertEquals(1, CropCatalog.recommend(PatchType.GRAPEVINE, 99).getQuantity());
		assertEquals(3, CropCatalog.recommend(PatchType.HOPS, 99).getQuantity());
		assertEquals(2, CropCatalog.recommend(PatchType.SEAWEED, 99).getQuantity());
	}

	@Test
	public void recommendsHighestUnlockedCrop()
	{
		assertEquals("Potato", CropCatalog.recommend(PatchType.ALLOTMENT, 1).getName());
		assertEquals("Watermelon", CropCatalog.recommend(PatchType.ALLOTMENT, 60).getName());
		assertEquals("Snape grass", CropCatalog.recommend(PatchType.ALLOTMENT, 61).getName());
		assertEquals("Torstol", CropCatalog.recommend(PatchType.HERB, 99).getName());
	}

	@Test
	public void returnsNullBelowPatchRequirement()
	{
		assertNull(CropCatalog.recommend(PatchType.REDWOOD, 89));
	}

	@Test
	public void keepsCorrectPlantingQuantity()
	{
		assertEquals(3, CropCatalog.recommend(PatchType.ALLOTMENT, 99).getQuantity());
		assertEquals(3, CropCatalog.recommend(PatchType.HOPS, 99).getQuantity());
		assertEquals(1, CropCatalog.recommend(PatchType.GRAPEVINE, 99).getQuantity());
	}

	@Test
	public void findsCropBySeedItem()
	{
		Crop crop = CropCatalog.recommend(PatchType.ALLOTMENT, 1);
		assertEquals(crop, CropCatalog.findByItemId(crop.getItemId()));
		assertNull(CropCatalog.findByItemId(-1));
	}
}
