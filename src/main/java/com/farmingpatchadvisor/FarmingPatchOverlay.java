package com.farmingpatchadvisor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.GameObject;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

final class FarmingPatchOverlay extends Overlay
{
	private final FarmingPatchAdvisorPlugin plugin;
	private final FarmingPatchAdvisorConfig config;
	private final FarmingLoadout farmingLoadout;

	@Inject
	private FarmingPatchOverlay(FarmingPatchAdvisorPlugin plugin, FarmingPatchAdvisorConfig config,
		FarmingLoadout farmingLoadout)
	{
		this.plugin = plugin;
		this.config = config;
		this.farmingLoadout = farmingLoadout;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPriority(PRIORITY_LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showPatchLabels())
		{
			return null;
		}

		List<PatchObject> remaining = new ArrayList<>();
		for (GameObject object : plugin.getPatchObjects())
		{
			if (object.getWorldView() == null || object.getPlane() != object.getWorldView().getPlane())
			{
				continue;
			}

			PatchType patchType = plugin.getPatchType(object);
			Crop crop = patchType == null ? null : farmingLoadout.recommendedCrop(patchType);
			if (crop == null)
			{
				continue;
			}

			remaining.add(new PatchObject(object, patchType, crop));
		}

		while (!remaining.isEmpty())
		{
			PatchObject first = remaining.remove(remaining.size() - 1);
			List<PatchObject> group = collectConnectedPatch(first, remaining);
			Area patchArea = new Area();
			for (PatchObject patchObject : group)
			{
				Shape clickbox = patchObject.object.getClickbox();
				if (clickbox != null)
				{
					patchArea.add(new Area(clickbox));
				}
			}

			if (!patchArea.isEmpty())
			{
				Color color = config.seedColor();
				OverlayUtil.renderPolygon(graphics, patchArea, color,
					new Color(color.getRed(), color.getGreen(), color.getBlue(), 25), new BasicStroke(2));

				String text = first.crop.getName() + " x" + first.crop.getQuantity()
					+ " (Lvl " + first.crop.getLevel() + ")";
				Rectangle bounds = patchArea.getBounds();
				int textX = bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(text)) / 2;
				int textY = bounds.y + bounds.height / 2;
				OverlayUtil.renderTextLocation(graphics, new Point(textX, textY), text, color);
			}
		}
		return null;
	}

	private static List<PatchObject> collectConnectedPatch(PatchObject first, List<PatchObject> remaining)
	{
		List<PatchObject> group = new ArrayList<>();
		ArrayDeque<PatchObject> queue = new ArrayDeque<>();
		Set<GameObject> queued = new HashSet<>();
		queue.add(first);
		queued.add(first.object);

		while (!queue.isEmpty())
		{
			PatchObject current = queue.removeFirst();
			group.add(current);
			Iterator<PatchObject> iterator = remaining.iterator();
			while (iterator.hasNext())
			{
				PatchObject candidate = iterator.next();
				if (isAdjacent(current, candidate) && queued.add(candidate.object))
				{
					queue.addLast(candidate);
					iterator.remove();
				}
			}
		}
		return group;
	}

	private static boolean isAdjacent(PatchObject first, PatchObject second)
	{
		if (first.patchType != second.patchType
			|| first.object.getWorldView() != second.object.getWorldView()
			|| first.object.getPlane() != second.object.getPlane())
		{
			return false;
		}

		WorldPoint a = first.object.getWorldLocation();
		WorldPoint b = second.object.getWorldLocation();
		return Math.abs(a.getX() - b.getX()) <= 1 && Math.abs(a.getY() - b.getY()) <= 1;
	}

	private static final class PatchObject
	{
		private final GameObject object;
		private final PatchType patchType;
		private final Crop crop;

		private PatchObject(GameObject object, PatchType patchType, Crop crop)
		{
			this.object = object;
			this.patchType = patchType;
			this.crop = crop;
		}
	}
}
