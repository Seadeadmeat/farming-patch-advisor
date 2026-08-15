package com.farmingpatchadvisor;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.plugins.bank.BankSearch;

@Singleton
final class BankChecklistFilter
{
	private static final int ACTIVE_COLOR = 0x46D37B;
	private static final int INACTIVE_COLOR = 0xFF981F;
	private static final int SIDE_RAIL_X = 1;
	private static final int SIDE_RAIL_WIDTH = 39;
	private static final int BUTTON_HEIGHT = 34;
	private static final int SCROLL_BUTTON_HEIGHT = 20;
	private static final int TAB_HEIGHT_WITH_MARGIN = 41;
	private static final int TAB_AREA_TOP = 61;
	private static final int BANK_BOTTOM_OFFSET = 39;
	private final Client client;
	private final BankSearch bankSearch;
	private final FarmingLoadout farmingLoadout;
	private final FarmingPatchAdvisorConfig config;
	private Widget button;
	private boolean active;

	@Inject
	private BankChecklistFilter(Client client, BankSearch bankSearch, FarmingLoadout farmingLoadout,
		FarmingPatchAdvisorConfig config)
	{
		this.client = client;
		this.bankSearch = bankSearch;
		this.farmingLoadout = farmingLoadout;
		this.config = config;
	}

	void addButton()
	{
		Widget bankRoot = client.getWidget(InterfaceID.Bankmain.UNIVERSE);
		Widget itemsContainer = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		if (bankRoot == null || itemsContainer == null)
		{
			return;
		}
		if (button != null && containsChild(bankRoot, button))
		{
			repositionButton();
			return;
		}
		button = bankRoot.createChild(-1, WidgetType.TEXT);
		button.setOriginalWidth(SIDE_RAIL_WIDTH);
		button.setOriginalHeight(BUTTON_HEIGHT);
		button.setFontId(FontID.BOLD_12);
		button.setXTextAlignment(1);
		button.setYTextAlignment(1);
		button.setTextShadowed(true);
		button.setText("Farm<br>Run");
		button.setTextColor(active ? ACTIVE_COLOR : INACTIVE_COLOR);
		button.setAction(0, active ? "Show all bank items" : "Show farm-run items");
		button.setHasListener(true);
		button.setNoClickThrough(true);
		button.setOnOpListener((JavaScriptCallback) event -> toggle());
		repositionButton();
	}

	void repositionButton()
	{
		Widget itemsContainer = client.getWidget(InterfaceID.Bankmain.ITEMS_CONTAINER);
		if (button == null || itemsContainer == null)
		{
			return;
		}

		int lowerBoundary = itemsContainer.getHeight() - BANK_BOTTOM_OFFSET;
		Widget incinerator = client.getWidget(InterfaceID.Bankmain.INCINERATOR_TARGET);
		if (incinerator != null && !incinerator.isHidden())
		{
			lowerBoundary = Math.min(lowerBoundary, incinerator.getRelativeY());
		}

		int tabCount = Math.max(0,
			(lowerBoundary - TAB_AREA_TOP - SCROLL_BUTTON_HEIGHT) / TAB_HEIGHT_WITH_MARGIN);
		int downArrowY = TAB_AREA_TOP + tabCount * TAB_HEIGHT_WITH_MARGIN + 1;
		button.setOriginalX(itemsContainer.getRelativeX() + SIDE_RAIL_X);
		button.setOriginalY(itemsContainer.getRelativeY()
			+ Math.max(TAB_AREA_TOP, downArrowY - BUTTON_HEIGHT - 2));
		button.revalidate();
	}

	private static boolean containsChild(Widget parent, Widget expectedChild)
	{
		Widget[] children = parent.getChildren();
		if (children == null)
		{
			return false;
		}
		for (Widget child : children)
		{
			if (child == expectedChild)
			{
				return true;
			}
		}
		return false;
	}

	void removeButton()
	{
		if (button != null)
		{
			button.setHidden(true);
			button = null;
		}
		if (active)
		{
			active = false;
			bankSearch.layoutBank();
		}
	}

	void onBankClosed()
	{
		button = null;
		active = false;
	}

	void filterBankItem()
	{
		if (!active)
		{
			return;
		}
		int[] stack = client.getIntStack();
		int size = client.getIntStackSize();
		if (size < 2)
		{
			return;
		}
		int itemId = stack[size - 1];
		Set<Integer> checklistIds = farmingLoadout.bankChecklistItemIds(
			config.includeCompost(), config.checklistTools(), config.checklistPayments(),
			ChecklistPatch.selected(config));
		stack[size - 2] = checklistIds.contains(itemId) ? 1 : 0;
	}

	private void toggle()
	{
		active = !active;
		if (button != null)
		{
			button.setTextColor(active ? ACTIVE_COLOR : INACTIVE_COLOR);
			button.setAction(0, active ? "Show all bank items" : "Show farm-run items");
		}
		bankSearch.layoutBank();
	}
}
