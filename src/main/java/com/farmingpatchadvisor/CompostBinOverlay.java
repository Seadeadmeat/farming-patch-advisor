package com.farmingpatchadvisor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

final class CompostBinOverlay extends Overlay
{
	private final Client client;
	private final FarmingPatchAdvisorConfig config;
	private final CompostBinManager manager;
	private final FarmRunFilterState filterState;

	@Inject
	private CompostBinOverlay(Client client, FarmingPatchAdvisorConfig config,
		CompostBinManager manager, FarmRunFilterState filterState)
	{
		this.client = client;
		this.config = config;
		this.manager = manager;
		this.filterState = filterState;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(PRIORITY_LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.enableCompostBins() || !config.highlightCompostBins()
			|| !filterState.includesCompost(config.includeCompostBinsInFarmRuns()))
		{
			return null;
		}
		Instant now = Instant.now();
		Set<CompostBinLocation> rendered = EnumSet.noneOf(CompostBinLocation.class);
		for (GameObject object : manager.getObjects())
		{
			if (object.getWorldView() == null || object.getPlane() != object.getWorldView().getPlane())
			{
				continue;
			}
			CompostBinLocation location = CompostBinLocation.at(object.getWorldLocation());
			if (location == null || !location.isEnabled(config) || !rendered.add(location))
			{
				continue;
			}
			CompostBinState state = manager.getState(location);
			Color color = stateColor(state, now);
			Area area = footprint(object);
			if (area.isEmpty())
			{
				continue;
			}
			Stroke oldStroke = graphics.getStroke();
			Color oldColor = graphics.getColor();
			graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
			graphics.fill(area);
			graphics.setColor(color);
			graphics.setStroke(new BasicStroke(2f));
			graphics.draw(area);
			graphics.setStroke(oldStroke);
			graphics.setColor(oldColor);

			Rectangle bounds = area.getBounds();
			String status = status(state, now);
			String action = manager.nextAction(location, state);
			int lineHeight = graphics.getFontMetrics().getHeight();
			renderCentered(graphics, bounds, location.getBinName(), bounds.y + bounds.height / 2 - lineHeight, color);
			renderCentered(graphics, bounds, status, bounds.y + bounds.height / 2, color);
			renderCentered(graphics, bounds, action, bounds.y + bounds.height / 2 + lineHeight, color);
		}
		return null;
	}

	private Area footprint(GameObject object)
	{
		Area area = new Area();
		Point min = object.getSceneMinLocation();
		Point max = object.getSceneMaxLocation();
		if (min == null || max == null)
		{
			min = max = new Point(object.getLocalLocation().getSceneX(), object.getLocalLocation().getSceneY());
		}
		for (int x = min.getX(); x <= max.getX(); x++)
		{
			for (int y = min.getY(); y <= max.getY(); y++)
			{
				java.awt.Polygon tile = Perspective.getCanvasTilePoly(client,
					LocalPoint.fromScene(x, y, object.getWorldView()), object.getPlane());
				if (tile != null)
				{
					area.add(new Area(tile));
				}
			}
		}
		return area;
	}

	private static Color stateColor(CompostBinState state, Instant now)
	{
		if (state == null || state.getPhase() == CompostBinPhase.EMPTY)
		{
			return Color.LIGHT_GRAY;
		}
		if (state.isReady(now) || state.getPhase() == CompostBinPhase.READY
			|| state.getPhase() == CompostBinPhase.COLLECTING)
		{
			return Color.GREEN;
		}
		return state.getPhase() == CompostBinPhase.FILLING ? Color.ORANGE : Color.WHITE;
	}

	private static String status(CompostBinState state, Instant now)
	{
		if (state == null)
		{
			return "Not inspected";
		}
		String remaining = CompostBinManager.remaining(state, now);
		if (remaining != null)
		{
			return remaining;
		}
		if (state.getPhase() == CompostBinPhase.FILLING)
		{
			return "Filling " + state.getAmount() + "/" + state.getLocation().getCapacity();
		}
		return state.getPhase().toString();
	}

	private static void renderCentered(Graphics2D graphics, Rectangle bounds, String text, int y, Color color)
	{
		int x = bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(text)) / 2;
		OverlayUtil.renderTextLocation(graphics, new Point(x, y), text, color);
	}
}
