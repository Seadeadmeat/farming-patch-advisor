package com.farmingpatchadvisor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

final class StorageScanReminderOverlay extends OverlayPanel
{
	private static final Color DARK_RED = new Color(145, 20, 20);
	private final FarmingLoadout farmingLoadout;
	private final FarmingPatchAdvisorConfig config;

	@Inject
	private StorageScanReminderOverlay(FarmingLoadout farmingLoadout, FarmingPatchAdvisorConfig config)
	{
		this.farmingLoadout = farmingLoadout;
		this.config = config;
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		boolean bankScanned = farmingLoadout.isBankScanned();
		boolean seedVaultScanned = farmingLoadout.isSeedVaultScanned();
		if (!config.showStorageScanReminder() || bankScanned && seedVaultScanned)
		{
			return null;
		}

		Color flashColor = (System.currentTimeMillis() / 500L) % 2 == 0 ? Color.RED : DARK_RED;
		String instruction = scanInstruction(bankScanned, seedVaultScanned);
		String detail = "for accurate patch recommendations.";
		FontMetrics metrics = graphics.getFontMetrics();
		int width = Math.max(metrics.stringWidth("Update Patch Recommendations"),
			Math.max(metrics.stringWidth(instruction), metrics.stringWidth(detail)));

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Update Patch Recommendations")
			.color(flashColor)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(instruction)
			.leftColor(flashColor)
			.build());
		panelComponent.getChildren().add(LineComponent.builder()
			.left(detail)
			.leftColor(flashColor)
			.build());
		panelComponent.setPreferredSize(new Dimension(width + 20, 0));
		return super.render(graphics);
	}

	static String scanInstruction(boolean bankScanned, boolean seedVaultScanned)
	{
		if (!bankScanned && !seedVaultScanned)
		{
			return "Open your bank and Seed Vault";
		}
		return bankScanned ? "Open your Seed Vault" : "Open your bank";
	}
}
