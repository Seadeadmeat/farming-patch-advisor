package com.farmingpatchadvisor;

import java.time.Instant;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PatchLocationCatalogTest
{
	@Test
	public void identifiesSharedFaladorAndPortSarimRegion()
	{
		assertEquals("Falador", PatchLocationCatalog.name(new WorldPoint(3050, 3300, 0)));
		assertEquals("Port Sarim", PatchLocationCatalog.name(new WorldPoint(3050, 3260, 0)));
	}

	@Test
	public void ordersHerbRunBeforeTreeRun()
	{
		Instant now = Instant.now();
		PatchTimer herb = new PatchTimer(new WorldPoint(3050, 3300, 0), PatchType.HERB,
			CropCatalog.recommend(PatchType.HERB, 99), now, now);
		PatchTimer tree = new PatchTimer(new WorldPoint(3200, 3200, 0), PatchType.TREE,
			CropCatalog.recommend(PatchType.TREE, 99), now, now);
		assertTrue(PatchLocationCatalog.routeRank(herb) < PatchLocationCatalog.routeRank(tree));
	}
}
