package com.farmingpatchadvisor;

import java.util.Locale;

final class PatchClassifier
{
	private PatchClassifier()
	{
	}

	static PatchType classify(String objectName)
	{
		if (objectName == null)
		{
			return null;
		}

		String name = objectName.toLowerCase(Locale.ENGLISH);
		if (name.contains("fruit tree patch"))
		{
			return PatchType.FRUIT_TREE;
		}
		if (name.contains("hardwood tree patch"))
		{
			return PatchType.HARDWOOD_TREE;
		}
		if (name.contains("crystal tree patch"))
		{
			return PatchType.CRYSTAL_TREE;
		}
		if (name.contains("spirit tree patch"))
		{
			return PatchType.SPIRIT_TREE;
		}
		if (name.contains("tree patch"))
		{
			return PatchType.TREE;
		}
		if (name.contains("allotment") || name.contains("vegetable patch"))
		{
			return PatchType.ALLOTMENT;
		}
		if (name.contains("flower patch"))
		{
			return PatchType.FLOWER;
		}
		if (name.contains("herb patch"))
		{
			return PatchType.HERB;
		}
		if (name.contains("hops patch") || name.contains("hop patch"))
		{
			return PatchType.HOPS;
		}
		if (name.contains("bush patch"))
		{
			return PatchType.BUSH;
		}
		if (name.contains("potato cactus patch") || name.contains("cactus patch"))
		{
			return PatchType.CACTUS;
		}
		if (name.contains("mushroom patch") || name.contains("bittercap patch"))
		{
			return PatchType.MUSHROOM;
		}
		if (name.contains("belladonna patch"))
		{
			return PatchType.BELLADONNA;
		}
		if (name.contains("calquat patch"))
		{
			return PatchType.CALQUAT;
		}
		if (name.contains("seaweed patch"))
		{
			return PatchType.SEAWEED;
		}
		if (name.contains("grapevine patch"))
		{
			return PatchType.GRAPEVINE;
		}
		if (name.contains("celastrus patch"))
		{
			return PatchType.CELASTRUS;
		}
		if (name.contains("redwood patch"))
		{
			return PatchType.REDWOOD;
		}
		if (name.contains("hespori patch"))
		{
			return PatchType.HESPORI;
		}
		if (name.contains("coral patch"))
		{
			return PatchType.CORAL;
		}
		return null;
	}

	static PatchType classifyGrowing(String objectName)
	{
		PatchType patch = classify(objectName);
		if (patch != null)
		{
			return patch;
		}
		String name = objectName == null ? "" : objectName.toLowerCase(Locale.ENGLISH);
		if (name.contains("herb"))
		{
			return PatchType.HERB;
		}
		Crop crop = CropCatalog.findInAnyText(name);
		return crop == null ? null : crop.getPatchType();
	}
}
