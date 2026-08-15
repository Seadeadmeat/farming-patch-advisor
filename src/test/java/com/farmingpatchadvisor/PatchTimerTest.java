package com.farmingpatchadvisor;

import java.time.Duration;
import java.time.Instant;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatchTimerTest
{
	@Test
	public void advancesInspectedStageFromElapsedTime()
	{
		Crop potato = CropCatalog.recommend(PatchType.ALLOTMENT, 1);
		Instant inspected = Instant.parse("2026-08-12T00:00:00Z");
		PatchTimer timer = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.ALLOTMENT, potato,
			inspected, inspected.plus(Duration.ofMinutes(30)), false, 2, 5);
		assertEquals(2, timer.getEstimatedStage(inspected.plus(Duration.ofMinutes(9))));
		assertEquals(3, timer.getEstimatedStage(inspected.plus(Duration.ofMinutes(10))));
		assertEquals(5, timer.getEstimatedStage(inspected.plus(Duration.ofHours(1))));
	}
}
