package com.farmingpatchadvisor;

import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FarmingContractCatalogTest
{
	@Test
	public void parsesTreeAssignment()
	{
		FarmingContract contract = FarmingContractCatalog.parseAssignment(
			"We need you to grow a Maple tree for us?");

		assertEquals("Maple tree", contract.getName());
		assertEquals(ItemID.PLANTPOT_MAPLE_SAPLING, contract.getCrop().getItemId());
		assertEquals(PatchType.TREE, contract.getCrop().getPatchType());
	}

	@Test
	public void parsesPluralBushAssignment()
	{
		FarmingContract contract = FarmingContractCatalog.parseAssignment(
			"Please could you grow some White berries for us.");

		assertEquals(ItemID.WHITEBERRY_BUSH_SEED, contract.getCrop().getItemId());
		assertEquals(PatchType.BUSH, contract.getCrop().getPatchType());
	}

	@Test
	public void acceptsWhiteLilySpellingVariants()
	{
		assertEquals(ItemID.WHITE_LILY_SEED,
			FarmingContractCatalog.findByName("White lillies").getCrop().getItemId());
		assertEquals(ItemID.WHITE_LILY_SEED,
			FarmingContractCatalog.findByName("White lilies").getCrop().getItemId());
	}

	@Test
	public void resolvesTimeTrackingProduceItem()
	{
		FarmingContract contract = FarmingContractCatalog.findByProduceItem(ItemID.REDWOOD_LOGS);

		assertEquals("Redwood tree", contract.getName());
		assertEquals(ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, contract.getCrop().getItemId());
	}

	@Test
	public void ignoresUnrelatedDialogue()
	{
		assertNull(FarmingContractCatalog.parseAssignment("Would you like another contract?"));
	}

	@Test
	public void detectsCompletionAndCancellation()
	{
		assertTrue(FarmingContractCatalog.isRewarded(
			"You'll be wanting a reward then. Here you go."));
		assertTrue(FarmingContractCatalog.isRewarded(
			"  You'll be wanting a reward then.   Here you go!  "));
		assertTrue(FarmingContractCatalog.isRewarded(
			"You have completed your farming contract."));
		assertTrue(FarmingContractCatalog.isRewarded(
			"You've completed a Farming Guild Contract. You should return to Guildmaster Jane."));
		assertTrue(FarmingContractCatalog.isNoContractDialogue(
			"Would you like another contract?"));
		assertTrue(FarmingContractCatalog.isNoContractDialogue(
			"Would you like a Farming contract?"));
		assertFalse(FarmingContractCatalog.isNoContractDialogue(
			"Your current contract is to grow some ranarr."));
		assertTrue(FarmingContractCatalog.isCancelled(
			"You have cancelled your current farming contract."));
		assertFalse(FarmingContractCatalog.isCancelled("Your contract is ready."));
	}
}
