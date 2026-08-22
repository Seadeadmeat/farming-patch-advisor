package com.farmingpatchadvisor;

import java.util.Locale;
import net.runelite.api.ObjectComposition;
import net.runelite.api.gameval.ObjectID;

final class PatchClassifier
{
	private static final int WEISS_HERB_PATCH = 33176;

	private PatchClassifier()
	{
	}

	static boolean isFarmRunPatchObject(int objectId)
	{
		switch (objectId)
		{
			case ObjectID.GARDEN_WHITE_TREE_PATCH:
			case ObjectID.GARDEN_WHITE_TREE_WEEDED:
			case ObjectID.GARDEN_WHITE_TREE_WEEDS_1:
			case ObjectID.GARDEN_WHITE_TREE_WEEDS_2:
			case ObjectID.GARDEN_WHITE_TREE_WEEDS_3:
			case ObjectID.GARDEN_WHITE_TREE_SEEDLING:
			case ObjectID.GARDEN_WHITE_TREE_1:
			case ObjectID.GARDEN_WHITE_TREE_2:
			case ObjectID.GARDEN_WHITE_TREE_3:
			case ObjectID.GARDEN_WHITE_TREE_FULLYGROWN:
			case ObjectID.GARDEN_WHITE_TREE_FRUIT_1:
			case ObjectID.GARDEN_WHITE_TREE_FRUIT_2:
			case ObjectID.GARDEN_WHITE_TREE_FRUIT_3:
			case ObjectID.GARDEN_WHITE_TREE_FRUIT_4:
			case ObjectID.GARDEN_WHITE_TREE_DEAD:
				return false;
			default:
				return true;
		}
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
		if (name.contains("redwood tree patch") || name.contains("redwood patch"))
		{
			return PatchType.REDWOOD;
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
		if (name.contains("mushroom patch") || name.contains("mushrooms patch")
			|| name.contains("bittercap patch"))
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
		if (name.contains("hespori patch"))
		{
			return PatchType.HESPORI;
		}
		if (name.contains("anima patch"))
		{
			return PatchType.ANIMA;
		}
		if (name.contains("coral patch"))
		{
			return PatchType.CORAL;
		}
		return null;
	}

	static PatchType classify(int objectId, String objectName)
	{
		return isSpecialHerbPatchObject(objectId) ? PatchType.HERB : classify(objectName);
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

	static PatchType classifyGrowing(int objectId, String objectName)
	{
		return isSpecialHerbPatchObject(objectId) ? PatchType.HERB : classifyGrowing(objectName);
	}

	static boolean isSpecialHerbPatchObject(int objectId)
	{
		return objectId == WEISS_HERB_PATCH
			|| objectId >= ObjectID.MYARM_HERBPATCH
			&& objectId <= ObjectID.MYARM_REALPATCH_HERB4_DEAD_ACTIVE;
	}

	static boolean hasAction(ObjectComposition composition, String expected)
	{
		return composition != null && hasAction(composition.getActions(), expected);
	}

	static boolean hasAction(String[] actions, String expected)
	{
		if (actions == null)
		{
			return false;
		}
		for (String action : actions)
		{
			if (expected.equalsIgnoreCase(action))
			{
				return true;
			}
		}
		return false;
	}
}
