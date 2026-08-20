package com.farmingpatchadvisor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemVariationMapping;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

final class FarmingItemOverlay extends WidgetItemOverlay
{
	private final FarmingPatchAdvisorConfig config;
	private final ItemManager itemManager;
	private final FarmingLoadout farmingLoadout;

	@Inject
	private FarmingItemOverlay(FarmingPatchAdvisorConfig config, ItemManager itemManager,
		FarmingLoadout farmingLoadout)
	{
		this.config = config;
		this.itemManager = itemManager;
		this.farmingLoadout = farmingLoadout;
		showOnBank();
		showOnInventory();
		showOnInterfaces(InterfaceID.SEED_VAULT);
	}

	@Override
	public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
	{
		int canonicalItemId = ItemVariationMapping.map(itemId);
		boolean seedVaultItem = isSeedVaultItem(widgetItem);
		Crop crop = (seedVaultItem
			? farmingLoadout.bestAvailableInSeedVaultByItemId()
			: farmingLoadout.bestAvailableByItemId()).get(itemId);
		boolean requiredItem = isRequiredItem(itemId)
			|| (canonicalItemId != itemId && isProtectionPayment(canonicalItemId));
		if (crop == null && !requiredItem)
		{
			return;
		}

		if ((!config.highlightBank() && isBankItem(widgetItem))
			|| (!config.highlightSeedVault() && seedVaultItem)
			|| (!config.highlightInventory() && !isBankItem(widgetItem) && !seedVaultItem))
		{
			return;
		}

		Color color = crop != null ? config.seedColor() : config.requiredItemColor();
		BufferedImage outline = itemManager.getItemOutline(itemId, widgetItem.getQuantity(), color);
		Rectangle bounds = widgetItem.getCanvasBounds();
		graphics.drawImage(outline, bounds.x, bounds.y, null);

	}

	private boolean isRequiredItem(int itemId)
	{
		if (farmingLoadout.isRemedyItem(ItemVariationMapping.map(itemId)))
		{
			return true;
		}
		if (config.checklistTools() && farmingLoadout.isRequiredToolItem(itemId))
		{
			return true;
		}
		if (isProtectionPayment(itemId))
		{
			return true;
		}
		return config.includeCompost()
			&& (itemId == ItemID.BUCKET_ULTRACOMPOST || itemId == ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED);
	}

	private boolean isProtectionPayment(int itemId)
	{
		if (!config.checklistPayments())
		{
			return false;
		}
		if (farmingLoadout.protectionPaymentItemIds(ChecklistPatch.selected(config)).contains(itemId))
		{
			return true;
		}
		return false;
	}

	private static boolean isBankItem(WidgetItem widgetItem)
	{
		int parentId = widgetItem.getWidget().getParentId();
		return parentId == InterfaceID.Bankmain.ITEMS || parentId == InterfaceID.SharedBank.ITEMS;
	}

	private static boolean isSeedVaultItem(WidgetItem widgetItem)
	{
		return WidgetUtil.componentToInterface(widgetItem.getWidget().getParentId()) == InterfaceID.SEED_VAULT;
	}
}
