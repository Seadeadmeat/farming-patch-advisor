package com.farmingpatchadvisor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.api.gameval.ItemID;

final class FarmingContractCatalog
{
	private static final Pattern ASSIGNMENT = Pattern.compile(
		"(?:We need you to grow|Please could you grow) (?:some|a|an) ([a-zA-Z ]+?)(?: for us)?[?.]",
		Pattern.CASE_INSENSITIVE);
	private static final String REWARD = "you'll be wanting a reward then. here you go";
	private static final Map<String, FarmingContract> BY_NAME;
	private static final Map<Integer, FarmingContract> BY_PRODUCE_ITEM;

	static
	{
		Map<String, FarmingContract> names = new HashMap<>();
		Map<Integer, FarmingContract> produce = new HashMap<>();
		add(names, produce, "Potatoes", ItemID.POTATO_SEED, ItemID.POTATO);
		add(names, produce, "Onions", ItemID.ONION_SEED, ItemID.ONION);
		add(names, produce, "Cabbages", ItemID.CABBAGE_SEED, ItemID.CABBAGE);
		add(names, produce, "Tomatoes", ItemID.TOMATO_SEED, ItemID.TOMATO);
		add(names, produce, "Sweetcorn", ItemID.SWEETCORN_SEED, ItemID.SWEETCORN);
		add(names, produce, "Strawberries", ItemID.STRAWBERRY_SEED, ItemID.STRAWBERRY);
		add(names, produce, "Watermelons", ItemID.WATERMELON_SEED, ItemID.WATERMELON);
		add(names, produce, "Snape grass", ItemID.SNAPE_GRASS_SEED, ItemID.SNAPE_GRASS);

		add(names, produce, "Marigolds", ItemID.MARIGOLD_SEED, ItemID.MARIGOLD);
		add(names, produce, "Rosemary", ItemID.ROSEMARY_SEED, ItemID.ROSEMARY);
		add(names, produce, "Nasturtiums", ItemID.NASTURTIUM_SEED, ItemID.NASTURTIUM);
		add(names, produce, "Woad", ItemID.WOAD_SEED, ItemID.WOADLEAF);
		add(names, produce, "Limpwurt roots", ItemID.LIMPWURT_SEED, ItemID.LIMPWURT_ROOT);
		add(names, produce, "White lillies", ItemID.WHITE_LILY_SEED, ItemID.WHITELILLY);
		names.put("white lilies", names.get("white lillies"));

		add(names, produce, "Redberries", ItemID.REDBERRY_BUSH_SEED, ItemID.REDBERRIES);
		add(names, produce, "Cadava berries", ItemID.CADAVABERRY_BUSH_SEED, ItemID.CADAVABERRIES);
		add(names, produce, "Dwellberries", ItemID.DWELLBERRY_BUSH_SEED, ItemID.DWELLBERRIES);
		add(names, produce, "Jangerberries", ItemID.JANGERBERRY_BUSH_SEED, ItemID.JANGERBERRIES);
		add(names, produce, "White berries", ItemID.WHITEBERRY_BUSH_SEED, ItemID.WHITE_BERRIES);
		add(names, produce, "Poison ivy berries", ItemID.POISONIVY_BUSH_SEED, ItemID.POISONIVY_BERRIES);

		add(names, produce, "Guam", ItemID.GUAM_SEED, ItemID.GUAM_LEAF);
		add(names, produce, "Marrentill", ItemID.MARRENTILL_SEED, ItemID.MARENTILL);
		add(names, produce, "Tarromin", ItemID.TARROMIN_SEED, ItemID.TARROMIN);
		add(names, produce, "Harralander", ItemID.HARRALANDER_SEED, ItemID.HARRALANDER);
		add(names, produce, "Ranarr", ItemID.RANARR_SEED, ItemID.RANARR_WEED);
		add(names, produce, "Toadflax", ItemID.TOADFLAX_SEED, ItemID.TOADFLAX);
		add(names, produce, "Irit", ItemID.IRIT_SEED, ItemID.IRIT_LEAF);
		add(names, produce, "Avantoe", ItemID.AVANTOE_SEED, ItemID.AVANTOE);
		add(names, produce, "Kwuarm", ItemID.KWUARM_SEED, ItemID.KWUARM);
		add(names, produce, "Snapdragon", ItemID.SNAPDRAGON_SEED, ItemID.SNAPDRAGON);
		add(names, produce, "Cadantine", ItemID.CADANTINE_SEED, ItemID.CADANTINE);
		add(names, produce, "Lantadyme", ItemID.LANTADYME_SEED, ItemID.LANTADYME);
		add(names, produce, "Dwarf weed", ItemID.DWARF_WEED_SEED, ItemID.DWARF_WEED);
		add(names, produce, "Torstol", ItemID.TORSTOL_SEED, ItemID.TORSTOL);

		add(names, produce, "Oak tree", ItemID.PLANTPOT_OAK_SAPLING, ItemID.OAK_LOGS);
		add(names, produce, "Willow tree", ItemID.PLANTPOT_WILLOW_SAPLING, ItemID.WILLOW_LOGS);
		add(names, produce, "Maple tree", ItemID.PLANTPOT_MAPLE_SAPLING, ItemID.MAPLE_LOGS);
		add(names, produce, "Yew tree", ItemID.PLANTPOT_YEW_SAPLING, ItemID.YEW_LOGS);
		add(names, produce, "Magic tree", ItemID.PLANTPOT_MAGIC_TREE_SAPLING, ItemID.MAGIC_LOGS);
		add(names, produce, "Apple tree", ItemID.PLANTPOT_APPLE_SAPLING, ItemID.COOKING_APPLE);
		add(names, produce, "Banana tree", ItemID.PLANTPOT_BANANA_SAPLING, ItemID.BANANA);
		add(names, produce, "Orange tree", ItemID.PLANTPOT_ORANGE_SAPLING, ItemID.ORANGE);
		add(names, produce, "Curry tree", ItemID.PLANTPOT_CURRY_SAPLING, ItemID.CURRY_LEAF);
		add(names, produce, "Pineapple plant", ItemID.PLANTPOT_PINEAPPLE_SAPLING, ItemID.PINEAPPLE);
		add(names, produce, "Papaya tree", ItemID.PLANTPOT_PAPAYA_SAPLING, ItemID.PAPAYA);
		add(names, produce, "Palm tree", ItemID.PLANTPOT_PALM_SAPLING, ItemID.COCONUT);
		add(names, produce, "Dragonfruit tree", ItemID.PLANTPOT_DRAGONFRUIT_SAPLING, ItemID.DRAGONFRUIT);

		add(names, produce, "Cactus", ItemID.CACTUS_SEED, ItemID.CACTUS_SPINE);
		add(names, produce, "Potato cacti", ItemID.POTATO_CACTUS_SEED, ItemID.CACTUS_POTATO);
		add(names, produce, "Celastrus tree", ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING, ItemID.BATTLESTAFF);
		add(names, produce, "Redwood tree", ItemID.PLANTPOT_REDWOOD_TREE_SAPLING, ItemID.REDWOOD_LOGS);

		BY_NAME = Collections.unmodifiableMap(names);
		BY_PRODUCE_ITEM = Collections.unmodifiableMap(produce);
	}

	private FarmingContractCatalog()
	{
	}

	static FarmingContract parseAssignment(String text)
	{
		if (text == null)
		{
			return null;
		}
		Matcher matcher = ASSIGNMENT.matcher(text);
		return matcher.find() ? findByName(matcher.group(1)) : null;
	}

	static FarmingContract findByName(String name)
	{
		return name == null ? null : BY_NAME.get(name.trim().toLowerCase(Locale.ENGLISH));
	}

	static FarmingContract findBySeedItem(int itemId)
	{
		Crop crop = CropCatalog.findByItemId(itemId);
		if (crop == null)
		{
			return null;
		}
		for (FarmingContract contract : BY_NAME.values())
		{
			if (contract.getCrop().equals(crop))
			{
				return contract;
			}
		}
		return null;
	}

	static FarmingContract findByProduceItem(int itemId)
	{
		return BY_PRODUCE_ITEM.get(itemId);
	}

	static boolean isRewarded(String text)
	{
		return normalize(text).contains(REWARD)
			|| normalize(text).contains("you have completed your farming contract")
			|| normalize(text).contains("you've completed your farming contract")
			|| normalize(text).contains("you've completed a farming guild contract");
	}

	static boolean isNoContractDialogue(String text)
	{
		String normalized = normalize(text);
		return normalized.contains("would you like another contract")
			|| normalized.contains("would you like a farming contract")
			|| normalized.contains("do you want a farming contract");
	}

	static boolean isCancelled(String text)
	{
		return text != null && text.contains("You have cancelled your current farming contract");
	}

	private static String normalize(String text)
	{
		return text == null ? "" : text.trim().toLowerCase(Locale.ENGLISH)
			.replaceAll("\\s+", " ");
	}

	private static void add(Map<String, FarmingContract> names, Map<Integer, FarmingContract> produce,
		String contractName, int seedItemId, int produceItemId)
	{
		Crop crop = CropCatalog.findByItemId(seedItemId);
		if (crop == null)
		{
			throw new IllegalStateException("Missing contract crop for seed item " + seedItemId);
		}
		FarmingContract contract = new FarmingContract(contractName, crop);
		names.put(contractName.toLowerCase(Locale.ENGLISH), contract);
		produce.put(produceItemId, contract);
	}
}
