package com.farmingpatchadvisor;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FarmingPatchAdvisorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FarmingPatchAdvisorPlugin.class);
		RuneLite.main(args);
	}
}
