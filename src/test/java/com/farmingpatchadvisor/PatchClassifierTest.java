package com.farmingpatchadvisor;

import net.runelite.api.gameval.ObjectID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PatchClassifierTest
{
	@Test
	public void recognizesPatchNames()
	{
		assertEquals(PatchType.HERB, PatchClassifier.classify("Herb patch"));
		assertEquals(PatchType.FRUIT_TREE, PatchClassifier.classify("Fruit tree patch"));
		assertEquals(PatchType.HARDWOOD_TREE, PatchClassifier.classify("Hardwood tree patch"));
		assertEquals(PatchType.REDWOOD, PatchClassifier.classify("Redwood tree patch"));
		assertEquals(PatchType.ALLOTMENT, PatchClassifier.classify("Allotment"));
		assertEquals(PatchType.CORAL, PatchClassifier.classify("Coral patch"));
	}

	@Test
	public void ignoresUnrelatedObjects()
	{
		assertNull(PatchClassifier.classify("Bank booth"));
		assertNull(PatchClassifier.classify(null));
	}

	@Test
	public void distinguishesPlantablePatchesFromDecorativeCropScenery()
	{
		assertTrue(PatchClassifier.hasAction(
			new String[]{"Inspect", "Guide", "Harvest", null, null}, "Guide"));
		assertFalse(PatchClassifier.hasAction(
			new String[]{"Pick", null, null, null, null}, "Guide"));
	}

	@Test
	public void recognizesGrowingCropNames()
	{
		assertEquals(PatchType.HERB, PatchClassifier.classifyGrowing("Herbs"));
		assertEquals(PatchType.ALLOTMENT, PatchClassifier.classifyGrowing("Strawberry plant"));
		assertEquals(PatchType.FLOWER, PatchClassifier.classifyGrowing("Flower patch"));
		assertEquals(PatchType.TREE, PatchClassifier.classifyGrowing("Magic tree"));
		assertEquals(PatchType.FRUIT_TREE, PatchClassifier.classifyGrowing("Dragonfruit tree"));
	}

	@Test
	public void excludesVarrockQuestWhiteTreePatch()
	{
		assertFalse(PatchClassifier.isFarmRunPatchObject(ObjectID.GARDEN_WHITE_TREE_PATCH));
		assertFalse(PatchClassifier.isFarmRunPatchObject(ObjectID.GARDEN_WHITE_TREE_FULLYGROWN));
		assertFalse(PatchClassifier.isFarmRunPatchObject(ObjectID.GARDEN_WHITE_TREE_FRUIT_4));
		assertFalse(PatchClassifier.isFarmRunPatchObject(ObjectID.GARDEN_WHITE_TREE_DEAD));
		assertTrue(PatchClassifier.isFarmRunPatchObject(ObjectID.TREE_PATCH_WEEDED));
	}
}
