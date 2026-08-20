package com.farmingpatchadvisor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

final class FarmingPatchOverlay extends Overlay
{
	private static final Color CONTRACT_CLEAR_COLOR = new Color(255, 152, 31);
	private final Client client;
	private final FarmingPatchAdvisorPlugin plugin;
	private final FarmingPatchAdvisorConfig config;
	private final FarmingLoadout farmingLoadout;
	private final FarmingContractManager contractManager;
	private final PatchTimerManager timerManager;

	@Inject
	private FarmingPatchOverlay(Client client, FarmingPatchAdvisorPlugin plugin, FarmingPatchAdvisorConfig config,
		FarmingLoadout farmingLoadout, FarmingContractManager contractManager,
		PatchTimerManager timerManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.farmingLoadout = farmingLoadout;
		this.contractManager = contractManager;
		this.timerManager = timerManager;
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
			Crop contractCrop = contractCrop(object, patchType);
			Crop crop = contractCrop != null ? contractCrop
				: patchType == null ? null : farmingLoadout.recommendedCrop(patchType);
			if (crop == null)
			{
				continue;
			}

			remaining.add(new PatchObject(object, patchType, crop, contractCrop != null));
		}

		while (!remaining.isEmpty())
		{
			PatchObject first = remaining.remove(remaining.size() - 1);
			List<PatchObject> group = collectConnectedPatch(first, remaining);
			PatchObject representative = group.stream().filter(patch -> patch.contractPatch)
				.findFirst().orElse(first);
			PatchTimer timer = timerManager.findTimer(representative.object.getWorldLocation(),
				representative.patchType, 4);
			Crop observedCrop = plugin.getPatchCrop(representative.object, representative.patchType);
			Crop currentCrop = observedCrop != null ? observedCrop : timer == null ? null : timer.getCrop();
			boolean clearForContract = representative.contractPatch
				&& requiresClearingForContract(currentCrop, representative.crop);
			Set<PatchTile> patchTiles = patchTiles(group);
			Area patchArea = canvasArea(patchTiles, representative.object);

			if (!patchArea.isEmpty())
			{
				Color color = timer != null && timer.isDead() ? Color.RED
					: timer != null && timer.isDiseased() ? Color.YELLOW
						: clearForContract ? CONTRACT_CLEAR_COLOR : config.seedColor();
				renderPatchBoundary(graphics, patchArea, patchTiles, representative.object, color);

				Rectangle bounds = patchArea.getBounds();
				List<String> lines = new ArrayList<>();
				if (clearForContract)
				{
					lines.add("Clear patch for contract");
					lines.add("Next: " + representative.crop.getItemName() + " x"
						+ representative.crop.getQuantity());
				}
				else
				{
					lines.add(representative.crop.getItemName() + " x" + representative.crop.getQuantity()
						+ " (Lvl " + representative.crop.getLevel() + ")");
				}
				boolean growing = timer != null && !timer.isDead() && !timer.isDiseased()
					&& java.time.Instant.now().isBefore(timer.getReadyAt());
				if (growing && (timer.needsCompost() || timer.needsWater()))
				{
					StringBuilder needs = new StringBuilder("Needs: ");
					if (timer.needsCompost())
					{
						needs.append("Compost");
					}
					if (timer.needsWater())
					{
						if (needs.length() > 7)
						{
							needs.append(" + ");
						}
						needs.append("Water");
					}
					lines.add(needs.toString());
				}
				int lineHeight = graphics.getFontMetrics().getHeight();
				int textY = bounds.y + bounds.height / 2 - (lines.size() - 1) * lineHeight / 2;
				for (String line : lines)
				{
					int textX = bounds.x + (bounds.width - graphics.getFontMetrics().stringWidth(line)) / 2;
					OverlayUtil.renderTextLocation(graphics, new Point(textX, textY), line, color);
					textY += lineHeight;
				}
			}
		}
		return null;
	}

	private Crop contractCrop(GameObject object, PatchType patchType)
	{
		if (patchType == null)
		{
			return null;
		}
		FarmingContract contract = config.showFarmingContract() && config.highlightContractPatch()
			? contractManager.getContract() : null;
		if (contract != null && contract.getCrop().getPatchType() == patchType
			&& "Farming Guild".equals(PatchLocationCatalog.name(object.getWorldLocation())))
		{
			PatchTimer plantedContract = plantedContractTimer(contract);
			return plantedContract == null
				|| belongsToPlantedContractPatch(object.getWorldLocation(), plantedContract.getPatchLocation())
				? contract.getCrop() : null;
		}
		return null;
	}

	private PatchTimer plantedContractTimer(FarmingContract contract)
	{
		for (PatchTimer timer : timerManager.getTimers())
		{
			if (!timer.isDead() && timer.getCrop().equals(contract.getCrop())
				&& "Farming Guild".equals(PatchLocationCatalog.name(timer.getPatchLocation())))
			{
				return timer;
			}
		}
		return null;
	}

	static boolean belongsToPlantedContractPatch(WorldPoint objectLocation, WorldPoint timerLocation)
	{
		return objectLocation.getPlane() == timerLocation.getPlane()
			&& Math.abs(objectLocation.getX() - timerLocation.getX()) <= 1
			&& Math.abs(objectLocation.getY() - timerLocation.getY()) <= 1;
	}

	static boolean requiresClearingForContract(Crop currentCrop, Crop contractCrop)
	{
		return currentCrop != null && contractCrop != null && !contractCrop.equals(currentCrop);
	}

	static boolean usesContractGroundOutline(boolean contractPatch, Crop currentCrop)
	{
		return contractPatch && currentCrop != null;
	}

	private Set<PatchTile> patchTiles(List<PatchObject> group)
	{
		Set<PatchTile> tiles = new HashSet<>();
		for (PatchObject patchObject : group)
		{
			Point min = patchObject.object.getSceneMinLocation();
			Point max = patchObject.object.getSceneMaxLocation();
			if (min == null || max == null)
			{
				LocalPoint local = patchObject.object.getLocalLocation();
				tiles.add(new PatchTile(local.getSceneX(), local.getSceneY()));
				continue;
			}
			for (int x = min.getX(); x <= max.getX(); x++)
			{
				for (int y = min.getY(); y <= max.getY(); y++)
				{
					tiles.add(new PatchTile(x, y));
				}
			}
		}
		return tiles;
	}

	private Area canvasArea(Set<PatchTile> tiles, GameObject reference)
	{
		Area area = new Area();
		for (PatchTile tile : tiles)
		{
			java.awt.Polygon polygon = Perspective.getCanvasTilePoly(client,
				LocalPoint.fromScene(tile.x, tile.y, reference.getWorldView()), reference.getPlane());
			if (polygon != null)
			{
				area.add(new Area(polygon));
			}
		}
		return area;
	}

	private void renderPatchBoundary(Graphics2D graphics, Area area, Set<PatchTile> tiles,
		GameObject reference, Color color)
	{
		Color previousColor = graphics.getColor();
		Stroke previousStroke = graphics.getStroke();
		graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
		graphics.fill(area);
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		for (PatchTile tile : tiles)
		{
			if (!tiles.contains(new PatchTile(tile.x, tile.y - 1)))
			{
				drawEdge(graphics, reference, tile.x, tile.y, tile.x + 1, tile.y);
			}
			if (!tiles.contains(new PatchTile(tile.x + 1, tile.y)))
			{
				drawEdge(graphics, reference, tile.x + 1, tile.y, tile.x + 1, tile.y + 1);
			}
			if (!tiles.contains(new PatchTile(tile.x, tile.y + 1)))
			{
				drawEdge(graphics, reference, tile.x + 1, tile.y + 1, tile.x, tile.y + 1);
			}
			if (!tiles.contains(new PatchTile(tile.x - 1, tile.y)))
			{
				drawEdge(graphics, reference, tile.x, tile.y + 1, tile.x, tile.y);
			}
		}
		graphics.setStroke(previousStroke);
		graphics.setColor(previousColor);
	}

	private void drawEdge(Graphics2D graphics, GameObject reference,
		int startX, int startY, int endX, int endY)
	{
		Point start = canvasCorner(reference, startX, startY);
		Point end = canvasCorner(reference, endX, endY);
		if (start != null && end != null)
		{
			graphics.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
		}
	}

	private Point canvasCorner(GameObject reference, int sceneX, int sceneY)
	{
		LocalPoint tileCenter = LocalPoint.fromScene(sceneX, sceneY, reference.getWorldView());
		return Perspective.localToCanvas(client,
			tileCenter.plus(-Perspective.LOCAL_HALF_TILE_SIZE, -Perspective.LOCAL_HALF_TILE_SIZE),
			reference.getPlane());
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

		Point firstMin = first.object.getSceneMinLocation();
		Point firstMax = first.object.getSceneMaxLocation();
		Point secondMin = second.object.getSceneMinLocation();
		Point secondMax = second.object.getSceneMaxLocation();
		if (firstMin != null && firstMax != null && secondMin != null && secondMax != null)
		{
			return sceneAreasTouch(firstMin, firstMax, secondMin, secondMax);
		}

		WorldPoint a = first.object.getWorldLocation();
		WorldPoint b = second.object.getWorldLocation();
		return Math.abs(a.getX() - b.getX()) <= 1 && Math.abs(a.getY() - b.getY()) <= 1;
	}

	static boolean sceneAreasTouch(Point firstMin, Point firstMax, Point secondMin, Point secondMax)
	{
		return firstMin.getX() <= secondMax.getX() + 1
			&& secondMin.getX() <= firstMax.getX() + 1
			&& firstMin.getY() <= secondMax.getY() + 1
			&& secondMin.getY() <= firstMax.getY() + 1;
	}

	private static final class PatchObject
	{
		private final GameObject object;
		private final PatchType patchType;
		private final Crop crop;
		private final boolean contractPatch;

		private PatchObject(GameObject object, PatchType patchType, Crop crop, boolean contractPatch)
		{
			this.object = object;
			this.patchType = patchType;
			this.crop = crop;
			this.contractPatch = contractPatch;
		}
	}

	private static final class PatchTile
	{
		private final int x;
		private final int y;

		private PatchTile(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object other)
		{
			return other instanceof PatchTile && ((PatchTile) other).x == x && ((PatchTile) other).y == y;
		}

		@Override
		public int hashCode()
		{
			return 31 * x + y;
		}
	}
}
