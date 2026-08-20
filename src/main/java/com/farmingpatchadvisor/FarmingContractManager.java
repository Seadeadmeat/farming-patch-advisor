package com.farmingpatchadvisor;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.util.Text;

@Singleton
final class FarmingContractManager
{
	private static final String CONFIG_GROUP = "farming-patch-advisor";
	private static final String CONTRACT_KEY = "farmingContractSeed";
	private static final String NO_CONTRACT = "none";
	private static final String TIME_TRACKING_GROUP = "timetracking";
	private static final String TIME_TRACKING_CONTRACT_KEY = "contract";

	private final Client client;
	private final ConfigManager configManager;
	private FarmingContract contract;

	@Inject
	private FarmingContractManager(Client client, ConfigManager configManager)
	{
		this.client = client;
		this.configManager = configManager;
	}

	FarmingContract getContract()
	{
		return contract;
	}

	void unload()
	{
		contract = null;
	}

	void load()
	{
		unload();
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
		String stored = configManager.getRSProfileConfiguration(CONFIG_GROUP, CONTRACT_KEY);
		if (NO_CONTRACT.equals(stored))
		{
			contract = null;
			return;
		}
		if (stored != null)
		{
			try
			{
				contract = FarmingContractCatalog.findBySeedItem(Integer.parseInt(stored));
				if (contract != null)
				{
					return;
				}
			}
			catch (NumberFormatException ignored)
			{
				// Fall through to RuneLite's Time Tracking value.
			}
		}

		String timeTracking = configManager.getRSProfileConfiguration(
			TIME_TRACKING_GROUP, TIME_TRACKING_CONTRACT_KEY);
		if (timeTracking != null)
		{
			try
			{
				contract = FarmingContractCatalog.findByProduceItem(Integer.parseInt(timeTracking));
				if (contract != null)
				{
					persist(contract);
				}
			}
			catch (NumberFormatException ignored)
			{
				contract = null;
			}
		}
	}

	boolean checkJaneDialogue()
	{
		Widget head = client.getWidget(InterfaceID.ChatLeft.HEAD);
		Widget text = client.getWidget(InterfaceID.ChatLeft.TEXT);
		if (head == null || text == null || head.getModelId() != NpcID.FARMING_GUILD_MASTER)
		{
			return false;
		}

		String dialogue = Text.removeTags(text.getText());
		FarmingContract parsed = FarmingContractCatalog.parseAssignment(dialogue);
		if (parsed != null)
		{
			return setContract(parsed);
		}
		return (FarmingContractCatalog.isRewarded(dialogue)
			|| FarmingContractCatalog.isNoContractDialogue(dialogue)) && setContract(null);
	}

	boolean checkGameMessage(String message)
	{
		return FarmingContractCatalog.isCancelled(Text.removeTags(message)) && setContract(null);
	}

	private boolean setContract(FarmingContract next)
	{
		if (sameContract(contract, next))
		{
			return false;
		}
		contract = next;
		persist(next);
		return true;
	}

	private void persist(FarmingContract value)
	{
		if (configManager.getRSProfileKey() == null)
		{
			return;
		}
		configManager.setRSProfileConfiguration(CONFIG_GROUP, CONTRACT_KEY,
			value == null ? NO_CONTRACT : Integer.toString(value.getCrop().getItemId()));
	}

	private static boolean sameContract(FarmingContract first, FarmingContract second)
	{
		return first == second || (first != null && second != null
			&& first.getCrop().equals(second.getCrop()));
	}
}
