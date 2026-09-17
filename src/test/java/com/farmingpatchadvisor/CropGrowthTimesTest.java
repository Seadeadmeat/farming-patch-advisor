package com.farmingpatchadvisor;

import java.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CropGrowthTimesTest
{
	@Test
	public void usesCropSpecificGrowthTimes()
	{
		assertEquals(Duration.ofMinutes(70), CropGrowthTimes.forCrop(CropCatalog.recommend(PatchType.ALLOTMENT, 61)));
		assertEquals(Duration.ofMinutes(480), CropGrowthTimes.forCrop(CropCatalog.recommend(PatchType.TREE, 75)));
		assertEquals(Duration.ofMinutes(5120), CropGrowthTimes.forCrop(CropCatalog.recommend(PatchType.ANIMA, 76)));
	}

	@Test
	public void calculatesMaximumRemainingFromInspectedStage()
	{
		Crop potato = CropCatalog.recommend(PatchType.ALLOTMENT, 1);
		assertEquals(Duration.ofMinutes(40), CropGrowthTimes.maximumRemainingAtStage(potato, 1, 5));
		assertEquals(Duration.ofMinutes(10), CropGrowthTimes.maximumRemainingAtStage(potato, 4, 5));
		assertEquals(Duration.ZERO, CropGrowthTimes.maximumRemainingAtStage(potato, 5, 5));
	}

	@Test
	public void calculatesStageDuration()
	{
		Crop potato = CropCatalog.recommend(PatchType.ALLOTMENT, 1);
		assertEquals(Duration.ofMinutes(10), CropGrowthTimes.stageDuration(potato, 5));
	}

	@Test
	public void hardwoodTimersUseTheCorrectNumberOfGrowthCycles()
	{
		String[] names = {"Teak sapling", "Mahogany sapling", "Camphor sapling",
			"Ironwood sapling", "Rosewood sapling"};
		int[] stages = {8, 9, 9, 9, 10};
		for (int i = 0; i < names.length; i++)
		{
			Crop crop = CropCatalog.findByName(PatchType.HARDWOOD_TREE, names[i]);
			Duration fullGrowth = Duration.ofMinutes((stages[i] - 1) * 640L);
			assertEquals(names[i], fullGrowth, CropGrowthTimes.forCrop(crop));
			assertEquals(names[i], Duration.ofMinutes(640), CropGrowthTimes.stageDuration(crop, stages[i]));
			assertEquals(names[i], fullGrowth,
				CropGrowthTimes.maximumRemainingAtStage(crop, 1, stages[i]));
			assertEquals(names[i], fullGrowth.minusMinutes(640),
				CropGrowthTimes.maximumRemainingAtStage(crop, 2, stages[i]));
			assertEquals(names[i], Duration.ZERO,
				CropGrowthTimes.maximumRemainingAtStage(crop, stages[i], stages[i]));
		}
	}
}
