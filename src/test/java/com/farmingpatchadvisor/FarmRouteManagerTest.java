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

	@Test public void customRouteCanKeepOnlyChosenPatches()
	{
		FarmRunPatch falador = new FarmRunPatch("Falador", "", PatchType.HERB);
		FarmRunPatch catherby = new FarmRunPatch("Catherby", "", PatchType.HERB);
		FarmRunPatch weiss = new FarmRunPatch("Weiss", "", PatchType.HERB);
		assertEquals(Arrays.asList(catherby, weiss), FarmRouteManager.included(
			Arrays.asList(falador, catherby, weiss),
			Arrays.asList(FarmRouteManager.key(catherby), FarmRouteManager.key(weiss))));
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

	@Test public void offersOnlyTeleportsThatServeTheSelectedPatchArea()
	{
		FarmRunPatch trollStronghold = new FarmRunPatch("Troll Stronghold", "", PatchType.HERB);
		List<RouteTeleport> trollOptions = Arrays.asList(RouteTeleport.viableValues(trollStronghold));
		assertTrue(trollOptions.contains(RouteTeleport.NONE));
		assertTrue(trollOptions.contains(RouteTeleport.STRONGHOLD));
		assertTrue(trollOptions.contains(RouteTeleport.TROLLHEIM));
		assertFalse(trollOptions.contains(RouteTeleport.ECTOPHIAL));
		assertFalse(trollOptions.contains(RouteTeleport.CATHERBY));

		FarmRunPatch catherby = new FarmRunPatch("Catherby", "", PatchType.HERB);
		List<RouteTeleport> catherbyOptions = Arrays.asList(RouteTeleport.viableValues(catherby));
		assertTrue(catherbyOptions.contains(RouteTeleport.CATHERBY));
		assertTrue(catherbyOptions.contains(RouteTeleport.CAMELOT));
		assertFalse(catherbyOptions.contains(RouteTeleport.STRONGHOLD));
	}

	@Test public void areasWithoutSupportedTravelItemsStillAllowNoItem()
	{
		FarmRunPatch unsupported = new FarmRunPatch("Unknown future area", "", PatchType.HERB);
		assertArrayEquals(new RouteTeleport[]{RouteTeleport.NONE}, RouteTeleport.viableValues(unsupported));
		FarmRunPatch sailingOnly = new FarmRunPatch("Anglers' Retreat", "", PatchType.HARDWOOD_TREE);
		assertArrayEquals(new RouteTeleport[]{RouteTeleport.NONE}, RouteTeleport.viableValues(sailingOnly));
	}
}
