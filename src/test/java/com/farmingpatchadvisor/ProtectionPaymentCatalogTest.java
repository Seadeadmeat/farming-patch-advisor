package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtectionPaymentCatalogTest
{
	@Test
	public void everyCatalogCropMatchesTheAuditedProtectionTable()
	{
		Map<Integer, List<ExpectedPayment>> expected = expectedPayments();
		Set<Integer> catalogItems = new HashSet<>();
		for (PatchType patchType : PatchType.values())
		{
			for (Crop crop : CropCatalog.crops(patchType))
			{
				catalogItems.add(crop.getItemId());
				List<ExpectedPayment> cropExpected = expected.get(crop.getItemId());
				List<ProtectionPayment> actual = ProtectionPaymentCatalog.forCrop(crop);
				int expectedSize = cropExpected == null ? 0 : cropExpected.size();
				assertEquals(crop.getName(), expectedSize, actual.size());
				for (int i = 0; i < expectedSize; i++)
				{
					ExpectedPayment wanted = cropExpected.get(i);
					ProtectionPayment found = actual.get(i);
					assertEquals(crop.getName(), wanted.name, found.getName());
					assertEquals(crop.getName(), wanted.itemId, found.getItemId());
					assertEquals(crop.getName(), wanted.quantity, found.getQuantity());
				}
			}
		}
		assertTrue("Every audited payment crop must exist in CropCatalog",
			catalogItems.containsAll(expected.keySet()));
		assertEquals("Audited payable crop count", 51, expected.size());
	}

	@Test
	public void nullCropHasNoPayment()
	{
		assertTrue(ProtectionPaymentCatalog.forCrop(null).isEmpty());
	}

	private static Map<Integer, List<ExpectedPayment>> expectedPayments()
	{
		Map<Integer, List<ExpectedPayment>> expected = new HashMap<>();

		// Allotments.
		expect(expected, ItemID.POTATO_SEED, "Compost", ItemID.BUCKET_COMPOST, 2);
		expect(expected, ItemID.ONION_SEED, "Potatoes(10)", ItemID.SACK_POTATO_10, 1);
		expect(expected, ItemID.CABBAGE_SEED, "Onions(10)", ItemID.SACK_ONION_10, 1);
		expect(expected, ItemID.TOMATO_SEED, "Cabbages(10)", ItemID.SACK_CABBAGE_10, 2);
		expect(expected, ItemID.SWEETCORN_SEED, "Jute fibre", ItemID.JUTE_FIBRE, 10);
		expect(expected, ItemID.STRAWBERRY_SEED, "Apples(5)", ItemID.BASKET_APPLE_5, 1);
		expect(expected, ItemID.WATERMELON_SEED, "Curry leaf", ItemID.CURRY_LEAF, 10);
		expect(expected, ItemID.SNAPE_GRASS_SEED, "Jangerberries", ItemID.JANGERBERRIES, 5);

		// Hops, including the Sailing crops.
		expect(expected, ItemID.BARLEY_SEED, "Compost", ItemID.BUCKET_COMPOST, 3);
		expect(expected, ItemID.HAMMERSTONE_HOP_SEED, "Marigolds", ItemID.MARIGOLD, 1);
		expect(expected, ItemID.ASGARNIAN_HOP_SEED, "Onions(10)", ItemID.SACK_ONION_10, 1);
		expect(expected, ItemID.JUTE_SEED, "Barley malt", ItemID.BARLEY_MALT, 6);
		expect(expected, ItemID.YANILLIAN_HOP_SEED, "Tomatoes(5)", ItemID.BASKET_TOMATO_5, 1);
		expect(expected, ItemID.FLAX_SEED, "Grain", ItemID.GRAIN, 6);
		expect(expected, ItemID.KRANDORIAN_HOP_SEED, "Cabbages(10)", ItemID.SACK_CABBAGE_10, 3);
		expect(expected, ItemID.WILDBLOOD_HOP_SEED, "Nasturtiums", ItemID.NASTURTIUM, 1);
		expect(expected, ItemID.HEMP_SEED, "Flax", ItemID.FLAX, 6);
		expect(expected, ItemID.COTTON_SEED, "Hemp", ItemID.HEMP, 6);

		// Bushes. Poison ivy is deliberately absent because it is disease-immune.
		expect(expected, ItemID.REDBERRY_BUSH_SEED, "Cabbages(10)", ItemID.SACK_CABBAGE_10, 4);
		expect(expected, ItemID.CADAVABERRY_BUSH_SEED, "Tomatoes(5)", ItemID.BASKET_TOMATO_5, 3);
		expect(expected, ItemID.DWELLBERRY_BUSH_SEED, "Strawberries(5)", ItemID.BASKET_STRAWBERRY_5, 3);
		expect(expected, ItemID.JANGERBERRY_BUSH_SEED, "Watermelon", ItemID.WATERMELON, 6);
		expect(expected, ItemID.WHITEBERRY_BUSH_SEED, "Bittercap mushroom", ItemID.BITTERCAP_MUSHROOM, 8);

		// Trees and fruit trees.
		expect(expected, ItemID.PLANTPOT_OAK_SAPLING, "Tomatoes(5)", ItemID.BASKET_TOMATO_5, 1);
		expect(expected, ItemID.PLANTPOT_WILLOW_SAPLING, "Apples(5)", ItemID.BASKET_APPLE_5, 1);
		expect(expected, ItemID.PLANTPOT_MAPLE_SAPLING, "Oranges(5)", ItemID.BASKET_ORANGE_5, 1);
		expect(expected, ItemID.PLANTPOT_YEW_SAPLING, "Cactus spine", ItemID.CACTUS_SPINE, 10);
		expect(expected, ItemID.PLANTPOT_MAGIC_TREE_SAPLING, "Coconut", ItemID.COCONUT, 25);
		expect(expected, ItemID.PLANTPOT_APPLE_SAPLING, "Sweetcorn", ItemID.SWEETCORN, 9);
		expect(expected, ItemID.PLANTPOT_BANANA_SAPLING, "Apples(5)", ItemID.BASKET_APPLE_5, 4);
		expect(expected, ItemID.PLANTPOT_ORANGE_SAPLING, "Strawberries(5)", ItemID.BASKET_STRAWBERRY_5, 3);
		expect(expected, ItemID.PLANTPOT_CURRY_SAPLING, "Bananas(5)", ItemID.BASKET_BANANA_5, 5);
		expect(expected, ItemID.PLANTPOT_PINEAPPLE_SAPLING, "Watermelon", ItemID.WATERMELON, 10);
		expect(expected, ItemID.PLANTPOT_PAPAYA_SAPLING, "Pineapple", ItemID.PINEAPPLE, 10);
		expect(expected, ItemID.PLANTPOT_PALM_SAPLING, "Papaya fruit", ItemID.PAPAYA, 15);
		expect(expected, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, "Coconut", ItemID.COCONUT, 15);

		// Hardwood and special trees.
		expect(expected, ItemID.PLANTPOT_TEAK_SAPLING, "Limpwurt root", ItemID.LIMPWURT_ROOT, 15);
		expect(expected, ItemID.PLANTPOT_MAHOGANY_SAPLING, "Yanillian hops", ItemID.YANILLIAN_HOPS, 25);
		expect(expected, ItemID.PLANTPOT_CAMPHOR_SAPLING, "White berries", ItemID.WHITE_BERRIES, 10);
		expect(expected, ItemID.PLANTPOT_IRONWOOD_SAPLING, "Curry leaf", ItemID.CURRY_LEAF, 10);
		expect(expected, ItemID.PLANTPOT_ROSEWOOD_SAPLING, "Dragonfruit", ItemID.DRAGONFRUIT, 8);
		expect(expected, ItemID.PLANTPOT_CALQUAT_SAPLING, "Poison ivy berries", ItemID.POISONIVY_BERRIES, 8);
		expect(expected, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING,
			"Monkey nuts", ItemID.MM_MONKEY_NUTS, 5,
			"Monkey bar", ItemID.MM_MONKEY_BAR, 1,
			"Ground tooth", ItemID.LUNAR_GROUNDTOOTH, 1);
		expect(expected, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, "Potato cactus", ItemID.CACTUS_POTATO, 8);
		expect(expected, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, "Dragonfruit", ItemID.DRAGONFRUIT, 6);

		// Cacti, seaweed, and coral.
		expect(expected, ItemID.CACTUS_SEED, "Cadava berries", ItemID.CADAVABERRIES, 6);
		expect(expected, ItemID.POTATO_CACTUS_SEED, "Snape grass", ItemID.SNAPE_GRASS, 8);
		expect(expected, ItemID.SEAWEED_SEED, "Numulite", ItemID.FOSSIL_NUMULITE, 200);
		expect(expected, ItemID.CORAL_ELKHORN_FRAG, "Giant seaweed", ItemID.GIANT_SEAWEED, 5);
		expect(expected, ItemID.CORAL_PILLAR_FRAG, "Elkhorn coral", ItemID.CORAL_ELKHORN, 5);
		expect(expected, ItemID.CORAL_UMBRAL_FRAG, "Pillar coral", ItemID.CORAL_PILLAR, 5);

		return expected;
	}

	private static void expect(Map<Integer, List<ExpectedPayment>> expected, int cropItemId,
		Object... paymentValues)
	{
		List<ExpectedPayment> payments = new ArrayList<>();
		for (int i = 0; i < paymentValues.length; i += 3)
		{
			payments.add(new ExpectedPayment((String) paymentValues[i],
				(Integer) paymentValues[i + 1], (Integer) paymentValues[i + 2]));
		}
		expected.put(cropItemId, payments);
	}

	private static final class ExpectedPayment
	{
		private final String name;
		private final int itemId;
		private final int quantity;

		private ExpectedPayment(String name, int itemId, int quantity)
		{
			this.name = name;
			this.itemId = itemId;
			this.quantity = quantity;
		}
	}
}
