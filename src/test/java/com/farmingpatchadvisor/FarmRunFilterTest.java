package com.farmingpatchadvisor;

import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FarmRunFilterTest
{
	@Test
	public void allIncludesEveryFarmRun()
	{
		for (FarmRunType runType : FarmRunType.values())
		{
			assertTrue(FarmRunFilter.ALL.includes(runType));
		}
	}

	@Test
	public void combinedTreeFilterOnlyIncludesTreeRuns()
	{
		assertTrue(FarmRunFilter.TREE_AND_FRUIT_TREE.includes(FarmRunType.TREE));
		assertTrue(FarmRunFilter.TREE_AND_FRUIT_TREE.includes(FarmRunType.FRUIT_TREE_CALQUAT));
		assertFalse(FarmRunFilter.TREE_AND_FRUIT_TREE.includes(FarmRunType.HARDWOOD_TREE));
		assertFalse(FarmRunFilter.TREE_AND_FRUIT_TREE.includes(FarmRunType.ALLOTMENT_FLOWER));
		assertFalse(FarmRunFilter.TREE_AND_FRUIT_TREE.includes(FarmRunType.HERB));
	}

	@Test
	public void filtersOnlyAppearWhenTheirEnabledRunsAreAvailable()
	{
		EnumSet<FarmRunType> treeOnly = EnumSet.of(FarmRunType.TREE);
		assertTrue(FarmRunFilter.ALL.isAvailable(treeOnly));
		assertTrue(FarmRunFilter.TREE.isAvailable(treeOnly));
		assertFalse(FarmRunFilter.FRUIT_TREE_CALQUAT.isAvailable(treeOnly));
		assertFalse(FarmRunFilter.TREE_AND_FRUIT_TREE.isAvailable(treeOnly));
		assertFalse(FarmRunFilter.COMPOST.isAvailable(treeOnly, false));
		assertTrue(FarmRunFilter.COMPOST.isAvailable(treeOnly, true));

		EnumSet<FarmRunType> bothTrees = EnumSet.of(FarmRunType.TREE, FarmRunType.FRUIT_TREE_CALQUAT);
		assertTrue(FarmRunFilter.TREE_AND_FRUIT_TREE.isAvailable(bothTrees));
	}

	@Test
	public void compostFilterDoesNotIncludeCropRuns()
	{
		for (FarmRunType runType : FarmRunType.values())
		{
			assertFalse(FarmRunFilter.COMPOST.includes(runType));
		}
		assertTrue(FarmRunFilter.COMPOST.includesCompost(false));
		assertFalse(FarmRunFilter.ALL.includesCompost(false));
		assertTrue(FarmRunFilter.ALL.includesCompost(true));
	}

	@Test
	public void routeEditorOffersEveryAvailableCropRun()
	{
		List<FarmRunFilter> filters = FarmRouteEditor.availableRuns(EnumSet.of(
			FarmRunType.HERB, FarmRunType.TREE, FarmRunType.FRUIT_TREE_CALQUAT));
		assertTrue(filters.contains(FarmRunFilter.ALL));
		assertTrue(filters.contains(FarmRunFilter.HERB));
		assertTrue(filters.contains(FarmRunFilter.TREE));
		assertTrue(filters.contains(FarmRunFilter.FRUIT_TREE_CALQUAT));
		assertTrue(filters.contains(FarmRunFilter.TREE_AND_FRUIT_TREE));
		assertFalse(filters.contains(FarmRunFilter.ALLOTMENT_FLOWER));
		assertFalse(filters.contains(FarmRunFilter.COMPOST));
	}
}
