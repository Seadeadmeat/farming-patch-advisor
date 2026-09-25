package com.farmingpatchadvisor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
final class CompostBinManager
{
	private static final String CONFIG_GROUP = "farming-patch-advisor";
	private static final String CONFIG_KEY = "compostBinStates";
	private static final Duration COMPOST_STAGE = Duration.ofMinutes(40);
	private final Client client;
	private final ConfigManager configManager;
	private final FarmingPatchAdvisorConfig config;
	private final Map<CompostBinLocation, CompostBinState> states = new EnumMap<>(CompostBinLocation.class);
	private final Set<GameObject> objects = new HashSet<>();

	@Inject
	private CompostBinManager(Client client, ConfigManager configManager, FarmingPatchAdvisorConfig config)
	{
		this.client = client;
		this.configManager = configManager;
		this.config = config;
	}

	synchronized void load()
	{
		states.clear();
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
		String stored = configManager.getRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY);
		if (stored == null || stored.isEmpty())
		{
			return;
		}
		for (String record : stored.split(";"))
		{
			try
			{
				String[] fields = record.split(",");
				CompostBinLocation location = CompostBinLocation.valueOf(fields[0]);
				CompostProduct product = CompostProduct.valueOf(fields[1]);
				CompostBinPhase phase = CompostBinPhase.valueOf(fields[2]);
				int amount = Integer.parseInt(fields[3]);
				int stage = Integer.parseInt(fields[4]);
				Instant observedAt = Instant.ofEpochMilli(Long.parseLong(fields[5]));
				Instant readyAt = "-".equals(fields[6]) ? null
					: Instant.ofEpochMilli(Long.parseLong(fields[6]));
				states.put(location, new CompostBinState(location, product, phase,
					amount, stage, observedAt, readyAt));
			}
			catch (RuntimeException ex)
			{
				log.debug("Ignoring malformed compost-bin state", ex);
			}
		}
	}

	synchronized void unload()
	{
		states.clear();
		objects.clear();
	}

	synchronized void clearObjects()
	{
		objects.clear();
	}

	synchronized void onGameObjectSpawned(GameObject object)
	{
		if (isCompostBinObject(object))
		{
			objects.add(object);
		}
	}

	synchronized void onGameObjectDespawned(GameObject object)
	{
		objects.remove(object);
	}

	synchronized Collection<GameObject> getObjects()
	{
		return Collections.unmodifiableList(new ArrayList<>(objects));
	}

	synchronized CompostBinState getState(CompostBinLocation location)
	{
		return states.get(location);
	}

	synchronized List<CompostBinLocation> enabledLocations()
	{
		List<CompostBinLocation> enabled = new ArrayList<>();
		if (!config.enableCompostBins())
		{
			return enabled;
		}
		for (CompostBinLocation location : CompostBinLocation.route())
		{
			if (location.isEnabled(config))
			{
				enabled.add(location);
			}
		}
		return enabled;
	}

	synchronized void observeCurrentLocation()
	{
		if (!config.enableCompostBins() || client.getGameState() != GameState.LOGGED_IN
			|| client.getLocalPlayer() == null)
		{
			return;
		}
		WorldPoint player = client.getLocalPlayer().getWorldLocation();
		CompostBinLocation location = CompostBinLocation.at(player);
		if (location == null || !location.isEnabled(config))
		{
			return;
		}
		int value = client.getVarbitValue(location.getVarbitId());
		CompostBinCodec.Decoded decoded = CompostBinCodec.decode(value, location.isBig());
		if (decoded.getPhase() == CompostBinPhase.UNKNOWN)
		{
			return;
		}
		Instant now = Instant.now();
		CompostBinState previous = states.get(location);
		Instant observedAt = now;
		Instant readyAt = readyAt(decoded, now);
		if (sameState(previous, decoded))
		{
			observedAt = previous.getObservedAt();
			readyAt = previous.getReadyAt();
		}
		CompostBinState updated = new CompostBinState(location, decoded.getProduct(), decoded.getPhase(),
			decoded.getAmount(), decoded.getStage(), observedAt, readyAt);
		if (!equivalent(previous, updated))
		{
			states.put(location, updated);
			save();
		}
	}

	synchronized void clear()
	{
		states.clear();
		save();
	}

	synchronized void reset(CompostBinLocation location)
	{
		states.remove(location);
		save();
	}

	synchronized CompostSupplyPlan supplyPlan()
	{
		int ingredients = 0;
		int ash = 0;
		int potions = 0;
		int buckets = 0;
		CompostTarget target = config.compostTarget();
		for (CompostBinLocation location : enabledLocations())
		{
			CompostBinState state = states.get(location);
			if (state == null || state.getPhase() == CompostBinPhase.EMPTY)
			{
				ingredients += location.getCapacity();
			}
			else if (state.getPhase() == CompostBinPhase.FILLING)
			{
				ingredients += Math.max(0, location.getCapacity() - state.getAmount());
			}
			if (target == CompostTarget.ULTRACOMPOST
				&& (state == null || state.getProduct() != CompostProduct.ULTRACOMPOST))
			{
				ash += location.isBig() ? 50 : 25;
			}
			if (target != CompostTarget.COMPOST && state != null
				&& state.getProduct() == CompostProduct.COMPOST
				&& state.getPhase() != CompostBinPhase.EMPTY && state.getPhase() != CompostBinPhase.FILLING)
			{
				potions++;
			}
			buckets = Math.max(buckets, location.getCapacity());
		}
		return new CompostSupplyPlan(ingredients, ash, potions, buckets);
	}

	String nextAction(CompostBinLocation location, CompostBinState state)
	{
		CompostTarget target = config.compostTarget();
		if (state == null)
		{
			return "Visit to inspect";
		}
		switch (state.getPhase())
		{
			case EMPTY:
				return "Add " + target.getIngredientName() + " 0/" + location.getCapacity();
			case FILLING:
				return state.getAmount() >= location.getCapacity() ? "Close lid"
					: "Add " + target.getIngredientName();
			case PROCESSING:
				return state.isReady(Instant.now()) ? "Open lid" : target == CompostTarget.ULTRACOMPOST
					? "Next: Add volcanic ash x" + (location.isBig() ? 50 : 25) : "Wait for compost";
			case READY:
			case COLLECTING:
				if (target != CompostTarget.COMPOST && state.getProduct() == CompostProduct.COMPOST)
				{
					return "Add compost potion";
				}
				if (target == CompostTarget.ULTRACOMPOST && state.getProduct() == CompostProduct.SUPERCOMPOST)
				{
					return "Add volcanic ash x" + (location.isBig() ? 50 : 25);
				}
				return "Collect with buckets";
			default:
				return "Inspect bin";
		}
	}

	static String remaining(CompostBinState state, Instant now)
	{
		if (state == null || state.getReadyAt() == null)
		{
			return null;
		}
		if (!now.isBefore(state.getReadyAt()))
		{
			return "READY";
		}
		return PatchTimerOverlay.formatRemaining(Duration.between(now, state.getReadyAt()));
	}

	private boolean isCompostBinObject(GameObject object)
	{
		CompostBinLocation location = CompostBinLocation.at(object.getWorldLocation());
		if (location == null)
		{
			return false;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition != null && composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}
		return composition != null && isCompostBinName(composition.getName());
	}

	static boolean isCompostBinName(String name)
	{
		return name != null && name.toLowerCase().contains("compost bin");
	}

	private static Instant readyAt(CompostBinCodec.Decoded decoded, Instant now)
	{
		if (decoded.getPhase() == CompostBinPhase.READY || decoded.getPhase() == CompostBinPhase.COLLECTING)
		{
			return now;
		}
		if (decoded.getPhase() != CompostBinPhase.PROCESSING || decoded.getStage() < 0)
		{
			return null;
		}
		int stagesRemaining = Math.max(0, 2 - decoded.getStage());
		return now.plus(COMPOST_STAGE.multipliedBy(stagesRemaining));
	}

	private static boolean sameState(CompostBinState previous, CompostBinCodec.Decoded decoded)
	{
		return previous != null && previous.getProduct() == decoded.getProduct()
			&& previous.getPhase() == decoded.getPhase() && previous.getAmount() == decoded.getAmount()
			&& previous.getStage() == decoded.getStage();
	}

	private static boolean equivalent(CompostBinState first, CompostBinState second)
	{
		return first != null && first.getProduct() == second.getProduct()
			&& first.getPhase() == second.getPhase() && first.getAmount() == second.getAmount()
			&& first.getStage() == second.getStage()
			&& java.util.Objects.equals(first.getReadyAt(), second.getReadyAt());
	}

	private void save()
	{
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
		StringBuilder value = new StringBuilder();
		for (CompostBinState state : states.values())
		{
			if (value.length() > 0) value.append(';');
			value.append(state.getLocation().name()).append(',')
				.append(state.getProduct().name()).append(',')
				.append(state.getPhase().name()).append(',')
				.append(state.getAmount()).append(',')
				.append(state.getStage()).append(',')
				.append(state.getObservedAt().toEpochMilli()).append(',')
				.append(state.getReadyAt() == null ? "-" : state.getReadyAt().toEpochMilli());
		}
		configManager.setRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY, value.toString());
	}
}
