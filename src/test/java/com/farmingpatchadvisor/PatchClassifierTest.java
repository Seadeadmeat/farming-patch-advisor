package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PatchClassifierTest
{
	@Test
	public void recognizesPatchNames()
	{
		assertEquals(PatchType.HERB, PatchClassifier.classify("Herb patch"));
		assertEquals(PatchType.FRUIT_TREE, PatchClassifier.classify("Fruit tree patch"));
		assertEquals(PatchType.HARDWOOD_TREE, PatchClassifier.classify("Hardwood tree patch"));
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
	public void recognizesGrowingCropNames()
	{
		assertEquals(PatchType.HERB, PatchClassifier.classifyGrowing("Herbs"));
		assertEquals(PatchType.TREE, PatchClassifier.classifyGrowing("Magic tree"));
		assertEquals(PatchType.FRUIT_TREE, PatchClassifier.classifyGrowing("Dragonfruit tree"));
	}
}
