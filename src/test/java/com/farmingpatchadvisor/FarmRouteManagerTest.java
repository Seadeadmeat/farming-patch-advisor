package com.farmingpatchadvisor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class FarmRouteManagerTest
{
	@Test public void restoresSavedOrderAndAppendsNewStops()
	{
		FarmRunPatch a = new FarmRunPatch("Falador", "", PatchType.HERB);
		FarmRunPatch b = new FarmRunPatch("Morytania", "", PatchType.HERB);
		FarmRunPatch c = new FarmRunPatch("Weiss", "", PatchType.HERB);
		assertEquals(Arrays.asList(b, a, c), FarmRouteManager.order(Arrays.asList(a, b, c),
			Arrays.asList(FarmRouteManager.key(b), "removed", FarmRouteManager.key(a))));
	}

	@Test public void savingFilteredRouteRetainsHiddenStopPositions()
	{
		assertEquals(Arrays.asList("c", "hidden", "a", "new"),
			FarmRouteManager.mergeOrder(Arrays.asList("c", "a", "new"), Arrays.asList("a", "hidden", "c")));
	}

	@Test public void sharedStopsUseOneTabletButSeparateVisitsNeedAnother()
	{
		FarmRunPatch herb = new FarmRunPatch("Catherby", "", PatchType.HERB);
		FarmRunPatch flower = new FarmRunPatch("Catherby", "", PatchType.FLOWER);
		FarmRunPatch other = new FarmRunPatch("Falador", "", PatchType.HERB);
		FarmRunPatch tree = new FarmRunPatch("Catherby", "", PatchType.FRUIT_TREE);
		Map<String, RouteTeleport> choices = new LinkedHashMap<>();
		for (FarmRunPatch p : Arrays.asList(herb, flower, tree))
		{
			choices.put(FarmRouteManager.key(p), RouteTeleport.CAMELOT);
		}
		assertEquals(Integer.valueOf(1), FarmRouteManager.requirements(Arrays.asList(herb, flower), choices).get(RouteTeleport.CAMELOT));
		assertEquals(Integer.valueOf(2), FarmRouteManager.requirements(Arrays.asList(herb, flower, other, tree), choices).get(RouteTeleport.CAMELOT));
		assertTrue(FarmRouteManager.requirements(Arrays.asList(herb), Collections.emptyMap()).isEmpty());
	}

	@Test public void reusableTeleportsAreNotMultipliedAndExcludeUnchargedJewellery()
	{
		FarmRunPatch a = new FarmRunPatch("Farming Guild", "", PatchType.HERB);
		FarmRunPatch b = new FarmRunPatch("Kourend", "", PatchType.HERB);
		Map<String, RouteTeleport> choices = new LinkedHashMap<>();
		choices.put(FarmRouteManager.key(a), RouteTeleport.SKILLS);
		choices.put(FarmRouteManager.key(b), RouteTeleport.SKILLS);
		assertEquals(Integer.valueOf(1), FarmRouteManager.requirements(Arrays.asList(a, b), choices).get(RouteTeleport.SKILLS));
		for (int id : RouteTeleport.SKILLS.itemIds())
		{
			assertNotEquals(net.runelite.api.gameval.ItemID.JEWL_NECKLACE_OF_SKILLS, id);
		}
	}
}
