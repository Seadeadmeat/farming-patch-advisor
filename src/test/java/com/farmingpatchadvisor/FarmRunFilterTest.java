package com.farmingpatchadvisor;

import java.util.EnumSet;
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

		EnumSet<FarmRunType> bothTrees = EnumSet.of(FarmRunType.TREE, FarmRunType.FRUIT_TREE_CALQUAT);
		assertTrue(FarmRunFilter.TREE_AND_FRUIT_TREE.isAvailable(bothTrees));
	}
}
