package com.farmingpatchadvisor;

import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChecklistPatchTest
{
	@Test
	public void individualPatchTogglesControlSelection()
	{
		FarmingPatchAdvisorConfig config = new FarmingPatchAdvisorConfig()
		{
			@Override
			public boolean checklistHerb()
			{
				return false;
			}
		};

		Set<ChecklistPatch> selected = ChecklistPatch.selected(config);
		assertFalse(selected.contains(ChecklistPatch.HERB));
		assertTrue(selected.contains(ChecklistPatch.ALLOTMENT));
		assertTrue(selected.contains(ChecklistPatch.TREE));
	}
}
