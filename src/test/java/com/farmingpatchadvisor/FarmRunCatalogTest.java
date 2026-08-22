package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FarmRunCatalogTest
{
	@Test
	public void placesGnomeStrongholdImmediatelyAfterVarrockInTreeRun()
	{
		List<String> locations = new ArrayList<>();
		for (FarmRunPatch patch : FarmRunCatalog.patches())
		{
			if (patch.getPatchType() == PatchType.TREE)
			{
				locations.add(patch.getLocation());
			}
		}
		assertEquals(Arrays.asList("Lumbridge", "Varrock", "Gnome Stronghold", "Falador",
			"Taverley", "Farming Guild", "Auburnvale"), locations);
	}

	@Test
	public void containsEveryStandardPlayerPlantablePatchFromRuneLiteAndWiki()
	{
		Map<PatchType, Integer> counts = new EnumMap<>(PatchType.class);
		for (FarmRunPatch patch : FarmRunCatalog.patches())
		{
			counts.merge(patch.getPatchType(), patch.getPatchCount(), Integer::sum);
		}

		assertEquals(Integer.valueOf(17), counts.get(PatchType.ALLOTMENT));
		assertEquals(Integer.valueOf(9), counts.get(PatchType.FLOWER));
		assertEquals(Integer.valueOf(10), counts.get(PatchType.HERB));
		assertEquals(Integer.valueOf(7), counts.get(PatchType.TREE));
		assertEquals(Integer.valueOf(7), counts.get(PatchType.FRUIT_TREE));
		assertEquals(Integer.valueOf(5), counts.get(PatchType.HARDWOOD_TREE));
		assertEquals(Integer.valueOf(5), counts.get(PatchType.HOPS));
		assertEquals(Integer.valueOf(5), counts.get(PatchType.BUSH));
		assertEquals(Integer.valueOf(2), counts.get(PatchType.CACTUS));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.MUSHROOM));
		assertEquals(Integer.valueOf(2), counts.get(PatchType.BELLADONNA));
		assertEquals(Integer.valueOf(3), counts.get(PatchType.CALQUAT));
		assertEquals(Integer.valueOf(5), counts.get(PatchType.SPIRIT_TREE));
		assertEquals(Integer.valueOf(2), counts.get(PatchType.SEAWEED));
		assertEquals(Integer.valueOf(12), counts.get(PatchType.GRAPEVINE));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.CELASTRUS));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.REDWOOD));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.HESPORI));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.ANIMA));
		assertEquals(Integer.valueOf(1), counts.get(PatchType.CRYSTAL_TREE));
		assertEquals(Integer.valueOf(2), counts.get(PatchType.CORAL));
	}

	@Test
	public void onlyAllowsPatchTypesAtDocumentedLocations()
	{
		assertTrue(FarmRunCatalog.hasPatch("Varrock", PatchType.TREE));
		assertTrue(FarmRunCatalog.hasPatch("Farming Guild", PatchType.ANIMA));
		assertTrue(FarmRunCatalog.hasPatch("Great Conch", PatchType.CORAL));
		assertFalse(FarmRunCatalog.hasPatch("Varrock", PatchType.FRUIT_TREE));
		assertFalse(FarmRunCatalog.hasPatch("Region 1234", PatchType.HERB));
	}
}
