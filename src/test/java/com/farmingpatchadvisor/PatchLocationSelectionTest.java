package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatchLocationSelectionTest
{
	@Test
	public void exclusionsApplyToGroupedAndOrdinaryLocations()
	{
		FarmingPatchAdvisorConfig config = new FarmingPatchAdvisorConfig()
		{
			@Override
			public boolean includeFossilIsland()
			{
				return false;
			}

			@Override
			public boolean includeVarlamore()
			{
				return false;
			}
		};

		assertFalse(PatchLocationSelection.isEnabled(config, "Fossil Island"));
		assertFalse(PatchLocationSelection.isEnabled(config, "Seaweed"));
		assertFalse(PatchLocationSelection.isEnabled(config, "Auburnvale"));
		assertFalse(PatchLocationSelection.isEnabled(config, "Civitas illa Fortis"));
		assertTrue(PatchLocationSelection.isEnabled(config, "Falador"));
		assertFalse(FarmRunCatalog.patches(config).stream()
			.anyMatch(patch -> patch.getLocation().equals("Fossil Island")
				|| patch.getLocation().equals("Seaweed")
				|| patch.getLocation().equals("Auburnvale")));
	}
}
