package com.farmingpatchadvisor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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

final class FarmRunChecklistOverlay extends OverlayPanel
{
	private final FarmingLoadout farmingLoadout;
	private final FarmingPatchAdvisorConfig config;
	private final Client client;

	@Inject
	private FarmRunChecklistOverlay(FarmingLoadout farmingLoadout, FarmingPatchAdvisorConfig config, Client client)
	{
		this.farmingLoadout = farmingLoadout;
		this.config = config;
		this.client = client;
		setPosition(OverlayPosition.TOP_RIGHT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_LOW);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showChecklistOverlay() || isStorageOpen())
		{
			return null;
		}
		List<FarmingLoadout.ChecklistItem> checklist = farmingLoadout.checklist(
			config.includeCompost(), config.checklistTools(), config.checklistPayments(),
			ChecklistPatch.selected(config));
		FontMetrics metrics = graphics.getFontMetrics();
		int contentWidth = metrics.stringWidth("Farm Run Checklist");
		panelComponent.getChildren().add(TitleComponent.builder().text("Farm Run Checklist").build());
		for (FarmingLoadout.ChecklistItem item : checklist)
		{
			boolean complete = item.getOwned() >= item.getNeeded();
			String amount = item.getOwned() + "/" + item.getNeeded();
			String label = (complete ? "[x] " : "[ ] ") + item.getName();
			contentWidth = Math.max(contentWidth, metrics.stringWidth(label) + metrics.stringWidth(amount) + 18);
			panelComponent.getChildren().add(LineComponent.builder()
				.left(label)
				.right(amount)
				.leftColor(complete ? Color.GREEN : (item.isSeed() ? config.seedColor() : config.requiredItemColor()))
				.rightColor(complete ? Color.GREEN : Color.RED)
				.build());
		}
		panelComponent.setPreferredSize(new Dimension(contentWidth + 20, 0));
		return super.render(graphics);
	}

	private boolean isStorageOpen()
	{
		Widget bank = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		Widget seedVault = client.getWidget(InterfaceID.SeedVault.UNIVERSE);
		return bank != null && !bank.isHidden() || seedVault != null && !seedVault.isHidden();
	}
}
