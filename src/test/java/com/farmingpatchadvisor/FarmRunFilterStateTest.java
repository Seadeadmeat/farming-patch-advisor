package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FarmRunFilterStateTest
{
	@Test
	public void selectedRunControlsIncludedChecklistPatchTypes()
	{
		FarmRunFilterState state = new FarmRunFilterState();
		assertTrue(state.includes(PatchType.HERB));
		assertTrue(state.includes(PatchType.TREE));

		state.setSelected(FarmRunFilter.HERB);
		assertTrue(state.includes(PatchType.HERB));
		assertFalse(state.includes(PatchType.ALLOTMENT));
		assertFalse(state.includes(PatchType.TREE));

		state.setSelected(FarmRunFilter.TREE_AND_FRUIT_TREE);
		assertTrue(state.includes(PatchType.TREE));
		assertTrue(state.includes(PatchType.FRUIT_TREE));
		assertTrue(state.includes(PatchType.CALQUAT));
		assertFalse(state.includes(PatchType.HARDWOOD_TREE));
	}
}
