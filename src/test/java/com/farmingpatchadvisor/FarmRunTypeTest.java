package com.farmingpatchadvisor;

import java.util.EnumSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
		assertEquals(FarmRunType.ALLOTMENT_FLOWER_HERB, FarmRunType.forPatchType(PatchType.HERB));
		assertEquals(FarmRunType.FRUIT_TREE_CALQUAT, FarmRunType.forPatchType(PatchType.CALQUAT));
		assertEquals(FarmRunType.SPECIALTY, FarmRunType.forPatchType(PatchType.SEAWEED));
	}
}
