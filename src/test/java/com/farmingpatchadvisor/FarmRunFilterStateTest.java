package com.farmingpatchadvisor;

import java.util.Set;
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

	@Test
	public void patchHighlightsRespectSettingsAndRunFilter()
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
		FarmRunFilterState state = new FarmRunFilterState();

		assertFalse(FarmingPatchOverlay.shouldHighlight(PatchType.HERB, selected, state));
		assertTrue(FarmingPatchOverlay.shouldHighlight(PatchType.TREE, selected, state));

		state.setSelected(FarmRunFilter.HERB);
		assertFalse(FarmingPatchOverlay.shouldHighlight(PatchType.TREE, selected, state));
		assertFalse(FarmingPatchOverlay.shouldHighlight(PatchType.HERB, selected, state));

		state.setSelected(FarmRunFilter.TREE_AND_FRUIT_TREE);
		assertTrue(FarmingPatchOverlay.shouldHighlight(PatchType.TREE, selected, state));
		assertTrue(FarmingPatchOverlay.shouldHighlight(PatchType.FRUIT_TREE, selected, state));
		assertFalse(FarmingPatchOverlay.shouldHighlight(PatchType.ALLOTMENT, selected, state));
	}
}
