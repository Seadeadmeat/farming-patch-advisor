package com.farmingpatchadvisor;

import java.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatchTimerOverlayTest
{
	@Test
	public void formatsCountdowns()
	{
		assertEquals("02:05", PatchTimerOverlay.formatRemaining(Duration.ofSeconds(125)));
		assertEquals("2:03:04", PatchTimerOverlay.formatRemaining(Duration.ofHours(2).plusMinutes(3).plusSeconds(4)));
		assertEquals("1d 02:03", PatchTimerOverlay.formatRemaining(Duration.ofDays(1).plusHours(2).plusMinutes(3)));
	}
}
