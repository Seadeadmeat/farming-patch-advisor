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
}
