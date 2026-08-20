package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
