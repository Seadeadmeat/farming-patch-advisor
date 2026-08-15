package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FarmRunCatalogTest
{
	@Test
	public void includesEverySupportedPatchCategoryAndMultiplePatchLocations()
	{
		assertTrue(FarmRunCatalog.patches().size() >= 60);
		for (PatchType patchType : PatchType.values())
		{
			assertTrue("Missing " + patchType, FarmRunCatalog.patches().stream()
				.anyMatch(patch -> patch.getPatchType() == patchType));
		}
	}

	@Test
	public void groupsRepeatedPatchesAtOneLocation()
	{
		FarmRunPatch grapes = FarmRunCatalog.patches().stream()
			.filter(patch -> patch.getLocation().equals("Kourend") && patch.getPatchType() == PatchType.GRAPEVINE)
			.findFirst().orElseThrow(AssertionError::new);
		assertTrue(grapes.getPatchCount() == 12);
		assertTrue(FarmRunCatalog.patches().stream()
			.filter(patch -> patch.getLocation().equals("Kourend") && patch.getPatchType() == PatchType.GRAPEVINE)
			.count() == 1);
	}
}
