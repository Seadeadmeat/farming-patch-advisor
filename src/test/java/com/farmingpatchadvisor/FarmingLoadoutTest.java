package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class FarmingLoadoutTest
{
	@Test
	public void fullRunIncludesMultipleSeedsForMultiSeedPatches()
	{
		int allotments = FarmRunCatalog.patches().stream()
			.filter(patch -> patch.getPatchType() == PatchType.ALLOTMENT)
			.mapToInt(FarmRunPatch::getPatchCount)
			.sum();
		assertTrue(allotments > 1);
		assertTrue(allotments * CropCatalog.recommend(PatchType.ALLOTMENT, 99).getQuantity() > allotments);
	}

	@Test
	public void plantingReducesOnlyTheStillNeededSide()
	{
		assertEquals(30, FarmingLoadout.stableOwnedSeedCount(30, true, 27));
		assertEquals(30, FarmingLoadout.stableOwnedSeedCount(30, true, 24));
		assertEquals(30, FarmingLoadout.stableOwnedSeedCount(27, true, 30));
		assertEquals(24, FarmingLoadout.stableOwnedSeedCount(30, false, 24));
		assertEquals(27, FarmingLoadout.remainingSeedQuantity(10, 1, 3));
		assertEquals(0, FarmingLoadout.remainingSeedQuantity(10, 10, 3));
	}
}
