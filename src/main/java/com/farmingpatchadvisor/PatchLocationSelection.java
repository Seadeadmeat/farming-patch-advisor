package com.farmingpatchadvisor;

final class PatchLocationSelection
{
	private PatchLocationSelection()
	{
	}

	static boolean isEnabled(FarmingPatchAdvisorConfig config, String location)
	{
		switch (location)
		{
			case "Morytania": return config.includeMorytania();
			case "Kourend": return config.includeKourend();
			case "Troll Stronghold": return config.includeTrollStronghold();
			case "Harmony Island": return config.includeHarmonyIsland();
			case "Weiss": return config.includeWeiss();
			case "Farming Guild": return config.includeFarmingGuild();
			case "Prifddinas": return config.includePrifddinas();
			case "Lletya": return config.includeLletya();
			case "Fossil Island":
			case "Seaweed": return config.includeFossilIsland();
			case "Etceteria": return config.includeEtceteria();
			case "Entrana": return config.includeEntrana();
			case "Aldarin":
			case "Auburnvale":
			case "Avium Savannah":
			case "Anglers' Retreat":
			case "Civitas illa Fortis":
			case "Kastori": return config.includeVarlamore();
			case "Great Conch": return config.includeGreatConch();
			default: return true;
		}
	}
}
