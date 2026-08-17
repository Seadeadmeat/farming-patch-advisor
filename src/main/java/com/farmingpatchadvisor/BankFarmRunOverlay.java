package com.farmingpatchadvisor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

final class BankFarmRunOverlay extends OverlayPanel
{
	private static final int PANEL_WIDTH = 190;
	private static final int PANEL_GAP = 6;
	private static final int SCREEN_MARGIN = 4;
	private static final int MAX_ITEMS = 14;

	private final Client client;
	private final FarmingLoadout farmingLoadout;
	private final FarmingPatchAdvisorConfig config;

	@Inject
	private BankFarmRunOverlay(Client client, FarmingLoadout farmingLoadout, FarmingPatchAdvisorConfig config)
	{
		this.client = client;
		this.farmingLoadout = farmingLoadout;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
		setPreferredSize(new Dimension(PANEL_WIDTH, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showBankChecklistPanel())
		{
			return null;
		}

		Widget storage = visibleWidget(InterfaceID.Bankmain.UNIVERSE);
		boolean seedVaultOpen = false;
		if (storage == null)
		{
			storage = visibleWidget(InterfaceID.SeedVault.UNIVERSE);
			seedVaultOpen = storage != null;
		}
		if (storage == null)
		{
			return null;
		}

		List<FarmingLoadout.ChecklistItem> checklist = farmingLoadout.checklist(
			config.includeCompost(), config.checklistTools(), config.checklistPayments(),
			ChecklistPatch.selected(config));
		List<FarmingLoadout.ChecklistItem> incomplete = new ArrayList<>();
		for (FarmingLoadout.ChecklistItem item : checklist)
		{
			if (item.getOwned() < item.getNeeded())
			{
				incomplete.add(item);
			}
		}

		int displayed = Math.min(MAX_ITEMS, incomplete.size());
		int renderedRows = incomplete.isEmpty() ? 1 : 1 + displayed + (incomplete.size() > displayed ? 1 : 0);
		positionNextTo(storage.getBounds(), renderedRows);
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(seedVaultOpen ? "Farm Run Seed List" : "Farm Run Bank List").build());
		if (incomplete.isEmpty())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("[x] Ready to leave")
				.leftColor(Color.GREEN)
				.build());
			return super.render(graphics);
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(incomplete.size() + " item" + (incomplete.size() == 1 ? "" : "s") + " remaining")
			.leftColor(Color.LIGHT_GRAY)
			.build());
		for (int i = 0; i < displayed; i++)
		{
			FarmingLoadout.ChecklistItem item = incomplete.get(i);
			panelComponent.getChildren().add(LineComponent.builder()
				.left("[ ] " + item.getName())
				.right(item.getOwned() + "/" + item.getNeeded())
				.leftColor(item.isSeed() ? config.seedColor() : config.requiredItemColor())
				.rightColor(Color.RED)
				.build());
		}
		if (incomplete.size() > displayed)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("+ " + (incomplete.size() - displayed) + " more (use Farm Run filter)")
				.leftColor(Color.GRAY)
				.build());
		}
		return super.render(graphics);
	}

	private Widget visibleWidget(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		return widget == null || widget.isHidden() ? null : widget;
	}

	private void positionNextTo(Rectangle bankBounds, int rows)
	{
		int spaceLeft = bankBounds.x - PANEL_GAP;
		int spaceRight = client.getCanvasWidth() - bankBounds.x - bankBounds.width - PANEL_GAP;
		boolean useLeft;
		switch (config.bankChecklistPosition())
		{
			case LEFT:
				useLeft = true;
				break;
			case RIGHT:
				useLeft = false;
				break;
			default:
				useLeft = spaceLeft >= PANEL_WIDTH || spaceLeft > spaceRight;
		}

		int x = useLeft
			? bankBounds.x - PANEL_WIDTH - PANEL_GAP
			: bankBounds.x + bankBounds.width + PANEL_GAP;
		x = Math.max(SCREEN_MARGIN, Math.min(x, client.getCanvasWidth() - PANEL_WIDTH - SCREEN_MARGIN));
		int estimatedHeight = 28 + rows * 18;
		int y;
		switch (config.bankChecklistVerticalPosition())
		{
			case MIDDLE:
				y = bankBounds.y + (bankBounds.height - estimatedHeight) / 2;
				break;
			case BOTTOM:
				y = bankBounds.y + bankBounds.height - estimatedHeight;
				break;
			default:
				y = bankBounds.y;
		}
		y = Math.max(SCREEN_MARGIN, Math.min(y, client.getCanvasHeight() - estimatedHeight - SCREEN_MARGIN));
		setPreferredLocation(new Point(x, y));
	}
}
