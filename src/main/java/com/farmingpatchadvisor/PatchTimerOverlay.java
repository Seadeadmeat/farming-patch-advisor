package com.farmingpatchadvisor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.FontMetrics;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

final class PatchTimerOverlay extends OverlayPanel
{
	private static final Color READY_DARK = new Color(140, 0, 0);
	private static final int MAX_CONTENT_WIDTH = 340;
	private final PatchTimerManager timerManager;
	private final FarmingPatchAdvisorConfig config;

	@Inject
	private PatchTimerOverlay(PatchTimerManager timerManager, FarmingPatchAdvisorConfig config)
	{
		this.timerManager = timerManager;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showTimerOverlay() || timerManager.getTimers().isEmpty())
		{
			return null;
		}

		Instant now = Instant.now();
		int contentWidth = graphics.getFontMetrics().stringWidth("Farming Patch Timers");
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Farming Patch Timers")
			.build());

		for (PatchTimer timer : timerManager.getTimers())
		{
			boolean ready = !timer.isDead() && !timer.isDiseased()
				&& !now.isBefore(timer.getReadyAt());
			Color readyColor = (System.currentTimeMillis() / 500L) % 2 == 0 ? Color.RED : READY_DARK;
			WorldPoint location = timer.getPatchLocation();
			String patch = timer.getCrop().getName() + " - " + PatchLocationCatalog.name(location);
			if (!timer.isPlantedTimer() && !timer.isDead() && !timer.isDiseased())
			{
				patch += " " + timer.getEstimatedStage(now) + "/" + timer.getTotalStages();
			}
			String remaining = timer.isDead() ? "DEAD" : timer.isDiseased() ? "DISEASED"
				: ready ? "READY" : formatRemaining(Duration.between(now, timer.getReadyAt()));
			FontMetrics metrics = graphics.getFontMetrics();
			Color stateColor = timer.isDead() ? Color.RED : timer.isDiseased() ? Color.YELLOW
				: ready ? readyColor : Color.WHITE;
			int combinedWidth = metrics.stringWidth(patch) + metrics.stringWidth(remaining) + 18;
			if (combinedWidth <= MAX_CONTENT_WIDTH)
			{
				contentWidth = Math.max(contentWidth, combinedWidth);
				panelComponent.getChildren().add(LineComponent.builder()
					.left(patch).right(remaining).leftColor(stateColor)
					.rightColor(timer.isDead() ? Color.RED : timer.isDiseased() ? Color.YELLOW
						: ready ? readyColor : Color.GREEN).build());
			}
			else
			{
				for (String line : wrapText(patch, metrics, MAX_CONTENT_WIDTH))
				{
					contentWidth = Math.max(contentWidth, metrics.stringWidth(line));
					panelComponent.getChildren().add(LineComponent.builder()
						.left(line).leftColor(stateColor).build());
				}
				contentWidth = Math.max(contentWidth, metrics.stringWidth(remaining));
				panelComponent.getChildren().add(LineComponent.builder().right(remaining)
					.rightColor(timer.isDead() ? Color.RED : timer.isDiseased() ? Color.YELLOW
						: ready ? readyColor : Color.GREEN).build());
			}
			String remedy = PatchRemedy.forTimer(timer);
			if (remedy != null)
			{
				String remedyText = "Remedy: " + remedy;
				for (String line : wrapText(remedyText, metrics, MAX_CONTENT_WIDTH))
				{
					contentWidth = Math.max(contentWidth, metrics.stringWidth(line));
					panelComponent.getChildren().add(LineComponent.builder()
						.left(line).leftColor(Color.LIGHT_GRAY).build());
				}
			}
		}
		panelComponent.setPreferredSize(new Dimension(contentWidth + 20, 0));

		return super.render(graphics);
	}

	static List<String> wrapText(String text, FontMetrics metrics, int maximumWidth)
	{
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split("\\s+"))
		{
			String candidate = line.length() == 0 ? word : line + " " + word;
			if (line.length() > 0 && metrics.stringWidth(candidate) > maximumWidth)
			{
				lines.add(line.toString());
				line.setLength(0);
			}
			if (line.length() > 0)
			{
				line.append(' ');
			}
			line.append(word);
		}
		if (line.length() > 0)
		{
			lines.add(line.toString());
		}
		return lines;
	}

	static String formatRemaining(Duration duration)
	{
		long seconds = Math.max(0, duration.getSeconds());
		long days = seconds / 86400;
		long hours = (seconds % 86400) / 3600;
		long minutes = (seconds % 3600) / 60;
		long secs = seconds % 60;
		if (days > 0)
		{
			return String.format("%dd %02d:%02d", days, hours, minutes);
		}
		if (hours > 0)
		{
			return String.format("%d:%02d:%02d", hours, minutes, secs);
		}
		return String.format("%02d:%02d", minutes, secs);
	}
}
