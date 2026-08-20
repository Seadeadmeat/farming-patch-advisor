package com.farmingpatchadvisor;

import java.util.EnumSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FarmRunTypeTest
{
	@Test
	public void categorizesEverySupportedPatchType()
	{
		EnumSet<PatchType> categorized = EnumSet.noneOf(PatchType.class);
		for (PatchType patchType : PatchType.values())
		{
			FarmRunType.forPatchType(patchType);
			categorized.add(patchType);
		}
		assertEquals(EnumSet.allOf(PatchType.class), categorized);
	}

	@Test
	public void usesExpectedFarmRunCategories()
	{
		assertEquals(FarmRunType.ALLOTMENT_FLOWER, FarmRunType.forPatchType(PatchType.ALLOTMENT));
		assertEquals(FarmRunType.ALLOTMENT_FLOWER, FarmRunType.forPatchType(PatchType.FLOWER));
		assertEquals(FarmRunType.HERB, FarmRunType.forPatchType(PatchType.HERB));
		assertEquals(FarmRunType.FRUIT_TREE_CALQUAT, FarmRunType.forPatchType(PatchType.CALQUAT));
		assertEquals(FarmRunType.SPECIALTY, FarmRunType.forPatchType(PatchType.SEAWEED));
	}

	@Test
	public void magicSecateursOnlyApplyToWikiListedYieldPatches()
	{
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.HERB));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.ALLOTMENT));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.HOPS));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.GRAPEVINE));
		assertFalse(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.TREE));
	}
}
