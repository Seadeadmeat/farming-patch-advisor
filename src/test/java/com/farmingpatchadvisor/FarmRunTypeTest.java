package com.farmingpatchadvisor;

import java.util.EnumSet;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FarmRunTypeTest
{
	@Test
	public void categorizesEverySupportedPatchType()
	{
		EnumSet<PatchType> categorized = EnumSet.noneOf(PatchType.class);
		for (PatchType patchType : PatchType.values())
		{
			FarmRunType.forPatchType(patchType);
			categorized.add(patchType);
		}
		assertEquals(EnumSet.allOf(PatchType.class), categorized);
	}

	@Test
	public void usesExpectedFarmRunCategories()
	{
		assertEquals(FarmRunType.ALLOTMENT_FLOWER, FarmRunType.forPatchType(PatchType.ALLOTMENT));
		assertEquals(FarmRunType.ALLOTMENT_FLOWER, FarmRunType.forPatchType(PatchType.FLOWER));
		assertEquals(FarmRunType.HERB, FarmRunType.forPatchType(PatchType.HERB));
		assertEquals(FarmRunType.FRUIT_TREE_CALQUAT, FarmRunType.forPatchType(PatchType.CALQUAT));
		assertEquals(FarmRunType.SPECIALTY, FarmRunType.forPatchType(PatchType.SEAWEED));
		assertEquals(FarmRunType.SPECIALTY, FarmRunType.forPatchType(PatchType.ANIMA));
	}

	@Test
	public void magicSecateursOnlyApplyToWikiListedYieldPatches()
	{
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.HERB));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.ALLOTMENT));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.HOPS));
		assertTrue(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.GRAPEVINE));
		assertFalse(FarmingLoadout.isYieldBoostedByMagicSecateurs(PatchType.TREE));
	}

	@Test
	public void usesCorrectCareFlagsForEveryPatchType()
	{
		EnumSet<PatchType> compostAndWater = EnumSet.of(
			PatchType.ALLOTMENT,
			PatchType.FLOWER,
			PatchType.HOPS);
		EnumSet<PatchType> compostOnly = EnumSet.of(
			PatchType.HERB,
			PatchType.BUSH,
			PatchType.TREE,
			PatchType.FRUIT_TREE,
			PatchType.HARDWOOD_TREE,
			PatchType.CACTUS,
			PatchType.MUSHROOM,
			PatchType.BELLADONNA,
			PatchType.CALQUAT,
			PatchType.SPIRIT_TREE,
			PatchType.SEAWEED,
			PatchType.CELASTRUS,
			PatchType.REDWOOD,
			PatchType.CRYSTAL_TREE,
			PatchType.CORAL);
		EnumSet<PatchType> noCareFlags = EnumSet.of(
			PatchType.GRAPEVINE,
			PatchType.HESPORI,
			PatchType.ANIMA);

		EnumSet<PatchType> audited = EnumSet.noneOf(PatchType.class);
		audited.addAll(compostAndWater);
		audited.addAll(compostOnly);
		audited.addAll(noCareFlags);
		assertEquals(EnumSet.allOf(PatchType.class), audited);

		for (PatchType patchType : PatchType.values())
		{
			assertEquals(compostAndWater.contains(patchType), patchType.canBeWatered());
			assertEquals(compostAndWater.contains(patchType) || compostOnly.contains(patchType),
				patchType.usesCompost());
		}
	}
}
