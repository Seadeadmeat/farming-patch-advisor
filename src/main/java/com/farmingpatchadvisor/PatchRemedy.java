package com.farmingpatchadvisor;

final class PatchRemedy
{
	private PatchRemedy()
	{
	}

	static String forTimer(PatchTimer timer)
	{
		if (timer.isDead())
		{
			return timer.getPatchType() == PatchType.REDWOOD
				? "Pay Alexandra to clear"
				: "Spade or Resurrect Crops";
		}
		if (!timer.isDiseased())
		{
			return null;
		}
		return usesSecateurs(timer.getPatchType())
			? "Secateurs / Plant cure / Cure Plant"
			: "Plant cure / Cure Plant";
	}

	static boolean usesSecateurs(PatchType patchType)
	{
		return patchType == PatchType.BUSH || patchType == PatchType.TREE
			|| patchType == PatchType.FRUIT_TREE || patchType == PatchType.HARDWOOD_TREE
			|| patchType == PatchType.SPIRIT_TREE || patchType == PatchType.CELASTRUS
			|| patchType == PatchType.REDWOOD;
	}
}
