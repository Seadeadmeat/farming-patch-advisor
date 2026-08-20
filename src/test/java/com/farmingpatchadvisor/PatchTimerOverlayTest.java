package com.farmingpatchadvisor;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.List;
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

	@Test
	public void wrapsLongStatusTextWithoutExceedingOverlayWidth()
	{
		Graphics2D graphics = new BufferedImage(500, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			FontMetrics metrics = graphics.getFontMetrics();
			List<String> lines = PatchTimerOverlay.wrapText(
				"Remedy: Secateurs / Plant cure / Cure Plant", metrics, 130);
			org.junit.Assert.assertTrue(lines.size() > 1);
			for (String line : lines)
			{
				org.junit.Assert.assertTrue(metrics.stringWidth(line) <= 130);
			}
		}
		finally
		{
			graphics.dispose();
		}
	}
}
