package com.farmingpatchadvisor;

import com.google.inject.Provides;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.ScriptID;
import net.runelite.api.Skill;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Farming Patch Advisor",
	description = "Recommends crops and highlights the seeds, saplings, and farming items needed to plant them",
	tags = {"farming", "seed", "patch", "bank", "highlight"}
)
public class FarmingPatchAdvisorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FarmingPatchOverlay patchOverlay;

	@Inject
	private FarmingItemOverlay itemOverlay;

	@Inject
	private PatchTimerOverlay timerOverlay;

	@Inject
	private FarmRunChecklistOverlay checklistOverlay;

	@Inject
	private BankFarmRunOverlay bankFarmRunOverlay;

	@Inject
	private FarmingLoadout farmingLoadout;

	@Inject
	private BankChecklistFilter bankChecklistFilter;

	@Inject
	private PatchTimerManager timerManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private FarmingPatchPanel patchPanel;

	@Inject
	private FarmingPatchAdvisorConfig config;

	private NavigationButton navigationButton;

	private final Set<GameObject> patchObjects = new HashSet<>();

	@Override
	protected void startUp()
	{
		timerManager.load();
		overlayManager.add(patchOverlay);
		overlayManager.add(itemOverlay);
		overlayManager.add(timerOverlay);
		overlayManager.add(checklistOverlay);
		overlayManager.add(bankFarmRunOverlay);
		patchPanel.start();
		navigationButton = NavigationButton.builder()
			.tooltip("Farming Patch Advisor")
			.icon(createNavigationIcon())
			.priority(8)
			.panel(patchPanel)
			.build();
		clientToolbar.addNavigation(navigationButton);
		bankChecklistFilter.addButton();
		log.debug("Farming Patch Advisor started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(patchOverlay);
		overlayManager.remove(itemOverlay);
		overlayManager.remove(timerOverlay);
		overlayManager.remove(checklistOverlay);
		overlayManager.remove(bankFarmRunOverlay);
		if (navigationButton != null)
		{
			clientToolbar.removeNavigation(navigationButton);
			navigationButton = null;
		}
		patchPanel.stop();
		bankChecklistFilter.removeButton();
		patchObjects.clear();
		log.debug("Farming Patch Advisor stopped");
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject object = event.getGameObject();
		if (getPatchType(object) != null)
		{
			patchObjects.add(object);
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		patchObjects.remove(event.getGameObject());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			patchObjects.clear();
			if (event.getGameState() == GameState.LOGIN_SCREEN)
			{
				farmingLoadout.clearStorageSnapshots();
			}
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		timerManager.onMenuOptionClicked(event);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		timerManager.onChatMessage(event);
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		farmingLoadout.onItemContainerChanged(event);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			bankChecklistFilter.addButton();
		}
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN)
		{
			bankChecklistFilter.onBankClosed();
		}
	}

	@Subscribe(priority = -200)
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if ("bankSearchFilter".equals(event.getEventName()))
		{
			bankChecklistFilter.filterBankItem();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.BANKMAIN_SIZE_CHECK
			|| event.getScriptId() == ScriptID.BANKMAIN_FINISHBUILDING
			|| event.getScriptId() == ScriptID.BANKMAIN_POPUP_TAB_DRAW)
		{
			bankChecklistFilter.addButton();
			bankChecklistFilter.repositionButton();
		}
	}

	Set<GameObject> getPatchObjects()
	{
		return Collections.unmodifiableSet(patchObjects);
	}

	PatchType getPatchType(GameObject object)
	{
		if (!PatchClassifier.isFarmRunPatchObject(object.getId()))
		{
			return null;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		PatchType patchType = composition == null ? null : PatchClassifier.classify(composition.getName());
		return patchType != null && PatchLocationSelection.isEnabled(config,
			PatchLocationCatalog.name(object.getWorldLocation())) ? patchType : null;
	}

	int getFarmingLevel()
	{
		return client.getGameState() == GameState.LOGGED_IN
			? client.getRealSkillLevel(Skill.FARMING)
			: 1;
	}

	WorldPoint getPatchAnchor(PatchType patchType, WorldPoint clicked)
	{
		GameObject seed = null;
		int nearest = Integer.MAX_VALUE;
		for (GameObject object : patchObjects)
		{
			if (getPatchType(object) != patchType || object.getPlane() != clicked.getPlane())
			{
				continue;
			}
			WorldPoint point = object.getWorldLocation();
			int distance = Math.max(Math.abs(point.getX() - clicked.getX()), Math.abs(point.getY() - clicked.getY()));
			if (distance < nearest)
			{
				nearest = distance;
				seed = object;
			}
		}
		if (seed == null || nearest > 4)
		{
			return clicked;
		}

		Set<GameObject> connected = new HashSet<>();
		ArrayDeque<GameObject> queue = new ArrayDeque<>();
		connected.add(seed);
		queue.add(seed);
		while (!queue.isEmpty())
		{
			GameObject current = queue.removeFirst();
			WorldPoint currentPoint = current.getWorldLocation();
			for (GameObject candidate : patchObjects)
			{
				if (connected.contains(candidate) || getPatchType(candidate) != patchType
					|| candidate.getWorldView() != current.getWorldView() || candidate.getPlane() != current.getPlane())
				{
					continue;
				}
				WorldPoint candidatePoint = candidate.getWorldLocation();
				if (Math.abs(currentPoint.getX() - candidatePoint.getX()) <= 1
					&& Math.abs(currentPoint.getY() - candidatePoint.getY()) <= 1)
				{
					connected.add(candidate);
					queue.addLast(candidate);
				}
			}
		}

		int x = connected.stream().mapToInt(object -> object.getWorldLocation().getX()).min().orElse(clicked.getX());
		int y = connected.stream().mapToInt(object -> object.getWorldLocation().getY()).min().orElse(clicked.getY());
		return new WorldPoint(x, y, clicked.getPlane());
	}

	@Provides
	FarmingPatchAdvisorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FarmingPatchAdvisorConfig.class);
	}

	private static BufferedImage createNavigationIcon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setColor(new Color(74, 45, 24));
		graphics.setStroke(new BasicStroke(2f));
		graphics.drawLine(8, 14, 8, 7);
		graphics.setColor(new Color(60, 190, 80));
		graphics.fillOval(2, 2, 7, 7);
		graphics.fillOval(7, 1, 7, 7);
		graphics.dispose();
		return image;
	}
}
