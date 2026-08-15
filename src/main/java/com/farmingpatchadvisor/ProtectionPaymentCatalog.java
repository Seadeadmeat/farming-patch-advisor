package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

final class ProtectionPaymentCatalog
{
	private static final Map<Integer, List<ProtectionPayment>> PAYMENTS = build();

	private ProtectionPaymentCatalog()
	{
	}

	static List<ProtectionPayment> forCrop(Crop crop)
	{
		return crop == null ? Collections.emptyList()
			: PAYMENTS.getOrDefault(crop.getItemId(), Collections.emptyList());
	}

	private static Map<Integer, List<ProtectionPayment>> build()
	{
		Map<Integer, List<ProtectionPayment>> payments = new HashMap<>();

		// Allotments.
		add(payments, ItemID.POTATO_SEED, payment("Compost", ItemID.BUCKET_COMPOST, 2));
		add(payments, ItemID.ONION_SEED, payment("Potatoes(10)", ItemID.SACK_POTATO_10, 1));
		add(payments, ItemID.CABBAGE_SEED, payment("Onions(10)", ItemID.SACK_ONION_10, 1));
		add(payments, ItemID.TOMATO_SEED, payment("Cabbages(10)", ItemID.SACK_CABBAGE_10, 2));
		add(payments, ItemID.SWEETCORN_SEED, payment("Jute fibre", ItemID.JUTE_FIBRE, 10));
		add(payments, ItemID.STRAWBERRY_SEED, payment("Apples(5)", ItemID.BASKET_APPLE_5, 1));
		add(payments, ItemID.WATERMELON_SEED, payment("Curry leaf", ItemID.CURRY_LEAF, 10));
		add(payments, ItemID.SNAPE_GRASS_SEED, payment("Jangerberries", ItemID.JANGERBERRIES, 5));

		// Hops.
		add(payments, ItemID.BARLEY_SEED, payment("Compost", ItemID.BUCKET_COMPOST, 3));
		add(payments, ItemID.HAMMERSTONE_HOP_SEED, payment("Marigolds", ItemID.MARIGOLD, 1));
		add(payments, ItemID.ASGARNIAN_HOP_SEED, payment("Onions(10)", ItemID.SACK_ONION_10, 1));
		add(payments, ItemID.JUTE_SEED, payment("Barley malt", ItemID.BARLEY_MALT, 6));
		add(payments, ItemID.YANILLIAN_HOP_SEED, payment("Tomatoes(5)", ItemID.BASKET_TOMATO_5, 1));
		add(payments, ItemID.KRANDORIAN_HOP_SEED, payment("Cabbages(10)", ItemID.SACK_CABBAGE_10, 3));
		add(payments, ItemID.WILDBLOOD_HOP_SEED, payment("Nasturtiums", ItemID.NASTURTIUM, 1));
		add(payments, ItemID.HEMP_SEED, payment("Flax", ItemID.FLAX, 6));
		add(payments, ItemID.COTTON_SEED, payment("Hemp", ItemID.HEMP, 6));

		// Bushes.
		add(payments, ItemID.REDBERRY_BUSH_SEED, payment("Cabbages(10)", ItemID.SACK_CABBAGE_10, 4));
		add(payments, ItemID.CADAVABERRY_BUSH_SEED, payment("Tomatoes(5)", ItemID.BASKET_TOMATO_5, 3));
		add(payments, ItemID.DWELLBERRY_BUSH_SEED, payment("Strawberries(5)", ItemID.BASKET_STRAWBERRY_5, 3));
		add(payments, ItemID.JANGERBERRY_BUSH_SEED, payment("Watermelons", ItemID.WATERMELON, 6));
		add(payments, ItemID.WHITEBERRY_BUSH_SEED, payment("Bittercap mushrooms", ItemID.BITTERCAP_MUSHROOM, 8));

		// Trees and fruit trees.
		add(payments, ItemID.PLANTPOT_OAK_SAPLING, payment("Tomatoes(5)", ItemID.BASKET_TOMATO_5, 1));
		add(payments, ItemID.PLANTPOT_WILLOW_SAPLING, payment("Apples(5)", ItemID.BASKET_APPLE_5, 1));
		add(payments, ItemID.PLANTPOT_MAPLE_SAPLING, payment("Oranges(5)", ItemID.BASKET_ORANGE_5, 1));
		add(payments, ItemID.PLANTPOT_YEW_SAPLING, payment("Cactus spines", ItemID.CACTUS_SPINE, 10));
		add(payments, ItemID.PLANTPOT_MAGIC_TREE_SAPLING, payment("Coconuts", ItemID.COCONUT, 25));
		add(payments, ItemID.PLANTPOT_APPLE_SAPLING, payment("Sweetcorn", ItemID.SWEETCORN, 9));
		add(payments, ItemID.PLANTPOT_BANANA_SAPLING, payment("Apples(5)", ItemID.BASKET_APPLE_5, 4));
		add(payments, ItemID.PLANTPOT_ORANGE_SAPLING, payment("Strawberries(5)", ItemID.BASKET_STRAWBERRY_5, 3));
		add(payments, ItemID.PLANTPOT_CURRY_SAPLING, payment("Bananas(5)", ItemID.BASKET_BANANA_5, 5));
		add(payments, ItemID.PLANTPOT_PINEAPPLE_SAPLING, payment("Watermelons", ItemID.WATERMELON, 10));
		add(payments, ItemID.PLANTPOT_PAPAYA_SAPLING, payment("Pineapples", ItemID.PINEAPPLE, 10));
		add(payments, ItemID.PLANTPOT_PALM_SAPLING, payment("Papaya fruit", ItemID.PAPAYA, 15));
		add(payments, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, payment("Coconuts", ItemID.COCONUT, 15));

		// Hardwood and special trees.
		add(payments, ItemID.PLANTPOT_TEAK_SAPLING, payment("Limpwurt roots", ItemID.LIMPWURT_ROOT, 15));
		add(payments, ItemID.PLANTPOT_MAHOGANY_SAPLING, payment("Yanillian hops", ItemID.YANILLIAN_HOPS, 25));
		add(payments, ItemID.PLANTPOT_CAMPHOR_SAPLING, payment("White berries", ItemID.WHITE_BERRIES, 10));
		add(payments, ItemID.PLANTPOT_IRONWOOD_SAPLING, payment("Curry leaf", ItemID.CURRY_LEAF, 10));
		add(payments, ItemID.PLANTPOT_ROSEWOOD_SAPLING, payment("Dragonfruit", ItemID.DRAGONFRUIT, 8));
		add(payments, ItemID.PLANTPOT_CALQUAT_SAPLING, payment("Poison ivy berries", ItemID.POISONIVY_BERRIES, 8));
		add(payments, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING,
			payment("Monkey nuts", ItemID.MM_MONKEY_NUTS, 5),
			payment("Monkey bar", ItemID.MM_MONKEY_BAR, 1),
			payment("Ground suqah tooth", ItemID.LUNAR_GROUNDTOOTH, 1));
		add(payments, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, payment("Potato cactus", ItemID.CACTUS_POTATO, 8));
		add(payments, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, payment("Dragonfruit", ItemID.DRAGONFRUIT, 6));

		// Cacti, seaweed, and coral. Flowers, herbs, mushrooms, belladonna,
		// grapevines, Hespori, crystal trees, and poison ivy have no payable protection.
		add(payments, ItemID.CACTUS_SEED, payment("Cadava berries", ItemID.CADAVABERRIES, 6));
		add(payments, ItemID.POTATO_CACTUS_SEED, payment("Snape grass", ItemID.SNAPE_GRASS, 8));
		add(payments, ItemID.SEAWEED_SEED, payment("Numulite", ItemID.FOSSIL_NUMULITE, 200));
		add(payments, ItemID.CORAL_ELKHORN_FRAG, payment("Giant seaweed", ItemID.GIANT_SEAWEED, 5));
		add(payments, ItemID.CORAL_PILLAR_FRAG, payment("Elkhorn coral", ItemID.CORAL_ELKHORN, 5));
		add(payments, ItemID.CORAL_UMBRAL_FRAG, payment("Pillar coral", ItemID.CORAL_PILLAR, 5));

		return Collections.unmodifiableMap(payments);
	}

	private static ProtectionPayment payment(String name, int itemId, int quantity)
	{
		return new ProtectionPayment(name, itemId, quantity);
	}

	private static void add(Map<Integer, List<ProtectionPayment>> payments, int cropItemId,
		ProtectionPayment... cropPayments)
	{
		List<ProtectionPayment> values = new ArrayList<>();
		Collections.addAll(values, cropPayments);
		payments.put(cropItemId, Collections.unmodifiableList(values));
	}
}
