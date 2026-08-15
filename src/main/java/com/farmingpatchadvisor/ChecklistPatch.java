package com.farmingpatchadvisor;

import java.util.EnumSet;
import java.util.Set;

public enum ChecklistPatch
{
	ALLOTMENT(PatchType.ALLOTMENT),
	FLOWER(PatchType.FLOWER),
	HERB(PatchType.HERB),
	HOPS(PatchType.HOPS),
	BUSH(PatchType.BUSH),
	TREE(PatchType.TREE),
	FRUIT_TREE(PatchType.FRUIT_TREE),
	HARDWOOD_TREE(PatchType.HARDWOOD_TREE),
	CACTUS(PatchType.CACTUS),
	MUSHROOM(PatchType.MUSHROOM),
	BELLADONNA(PatchType.BELLADONNA),
	CALQUAT(PatchType.CALQUAT),
	SPIRIT_TREE(PatchType.SPIRIT_TREE),
	SEAWEED(PatchType.SEAWEED),
	GRAPEVINE(PatchType.GRAPEVINE),
	CELASTRUS(PatchType.CELASTRUS),
	REDWOOD(PatchType.REDWOOD),
	HESPORI(PatchType.HESPORI),
	CRYSTAL_TREE(PatchType.CRYSTAL_TREE),
	CORAL(PatchType.CORAL);

	private final PatchType patchType;

	ChecklistPatch(PatchType patchType)
	{
		this.patchType = patchType;
	}

	PatchType getPatchType()
	{
		return patchType;
	}

	static Set<ChecklistPatch> selected(FarmingPatchAdvisorConfig config)
	{
		EnumSet<ChecklistPatch> selected = EnumSet.noneOf(ChecklistPatch.class);
		for (ChecklistPatch patch : values())
		{
			if (patch.isEnabled(config))
			{
				selected.add(patch);
			}
		}
		return selected;
	}

	private boolean isEnabled(FarmingPatchAdvisorConfig config)
	{
		switch (this)
		{
			case ALLOTMENT: return config.checklistAllotment();
			case FLOWER: return config.checklistFlower();
			case HERB: return config.checklistHerb();
			case HOPS: return config.checklistHops();
			case BUSH: return config.checklistBush();
			case TREE: return config.checklistTree();
			case FRUIT_TREE: return config.checklistFruitTree();
			case HARDWOOD_TREE: return config.checklistHardwoodTree();
			case CACTUS: return config.checklistCactus();
			case MUSHROOM: return config.checklistMushroom();
			case BELLADONNA: return config.checklistBelladonna();
			case CALQUAT: return config.checklistCalquat();
			case SPIRIT_TREE: return config.checklistSpiritTree();
			case SEAWEED: return config.checklistSeaweed();
			case GRAPEVINE: return config.checklistGrapevine();
			case CELASTRUS: return config.checklistCelastrus();
			case REDWOOD: return config.checklistRedwood();
			case HESPORI: return config.checklistHespori();
			case CRYSTAL_TREE: return config.checklistCrystalTree();
			case CORAL: return config.checklistCoral();
			default: return true;
		}
	}

	@Override
	public String toString()
	{
		return patchType.getDisplayName();
	}
}
