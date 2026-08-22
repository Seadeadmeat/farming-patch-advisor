package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

final class CropCatalog
{
	private static final Map<PatchType, List<Crop>> CROPS = buildCatalog();

	private CropCatalog()
	{
	}

	static Crop recommend(PatchType patchType, int farmingLevel)
	{
		Crop recommendation = null;
		for (Crop crop : CROPS.getOrDefault(patchType, Collections.emptyList()))
		{
			if (crop.getLevel() <= farmingLevel
				&& (recommendation == null || crop.getLevel() > recommendation.getLevel()))
			{
				recommendation = crop;
			}
		}
		return recommendation;
	}

	static List<Crop> recommendations(int farmingLevel)
	{
		List<Crop> recommendations = new ArrayList<>();
		for (PatchType patchType : PatchType.values())
		{
			Crop crop = recommend(patchType, farmingLevel);
			if (crop != null)
			{
				recommendations.add(crop);
			}
		}
		return recommendations;
	}

	static List<Crop> crops(PatchType patchType)
	{
		return CROPS.getOrDefault(patchType, Collections.emptyList());
	}

	static Crop findByItemId(int itemId)
	{
		for (List<Crop> crops : CROPS.values())
		{
			for (Crop crop : crops)
			{
				if (crop.getItemId() == itemId)
				{
					return crop;
				}
			}
		}
		return null;
	}

	static Crop findByName(PatchType patchType, String name)
	{
		for (Crop crop : CROPS.getOrDefault(patchType, Collections.emptyList()))
		{
			if (crop.getName().equals(name) || crop.getItemName().equals(name))
			{
				return crop;
			}
		}
		return null;
	}

	static Crop findInText(String text, PatchType patchType)
	{
		String normalized = text.toLowerCase();
		Crop best = null;
		int bestLength = 0;
		for (Crop crop : CROPS.getOrDefault(patchType, Collections.emptyList()))
		{
			String name = crop.getName().toLowerCase().replace(" sapling", "");
			if (normalized.contains(name) && name.length() > bestLength)
			{
				best = crop;
				bestLength = name.length();
			}
		}
		return best;
	}

	static Crop findInAnyText(String text)
	{
		Crop best = null;
		for (PatchType patchType : PatchType.values())
		{
			Crop candidate = findInText(text, patchType);
			if (candidate != null && (best == null || candidate.getName().length() > best.getName().length()))
			{
				best = candidate;
			}
		}
		return best;
	}

	private static Map<PatchType, List<Crop>> buildCatalog()
	{
		Map<PatchType, List<Crop>> crops = new EnumMap<>(PatchType.class);

		add(crops, PatchType.ALLOTMENT, 3,
			seed("Potato", 1, ItemID.POTATO_SEED),
			seed("Onion", 5, ItemID.ONION_SEED),
			seed("Cabbage", 7, ItemID.CABBAGE_SEED),
			seed("Tomato", 12, ItemID.TOMATO_SEED),
			seed("Sweetcorn", 20, ItemID.SWEETCORN_SEED),
			seed("Strawberry", 31, ItemID.STRAWBERRY_SEED),
			seed("Watermelon", 47, ItemID.WATERMELON_SEED),
			seed("Snape grass", 61, ItemID.SNAPE_GRASS_SEED));

		add(crops, PatchType.FLOWER, 1,
			seed("Marigold", 2, ItemID.MARIGOLD_SEED),
			seed("Rosemary", 11, ItemID.ROSEMARY_SEED),
			seed("Nasturtium", 24, ItemID.NASTURTIUM_SEED),
			seed("Woad", 25, ItemID.WOAD_SEED),
			seed("Limpwurt", 26, ItemID.LIMPWURT_SEED),
			seed("White lily", 58, ItemID.WHITE_LILY_SEED));

		add(crops, PatchType.HERB, 1,
			seed("Guam", 9, ItemID.GUAM_SEED),
			seed("Marrentill", 14, ItemID.MARRENTILL_SEED),
			seed("Tarromin", 19, ItemID.TARROMIN_SEED),
			seed("Harralander", 26, ItemID.HARRALANDER_SEED),
			seed("Ranarr", 32, ItemID.RANARR_SEED),
			seed("Toadflax", 38, ItemID.TOADFLAX_SEED),
			seed("Irit", 44, ItemID.IRIT_SEED),
			seed("Avantoe", 50, ItemID.AVANTOE_SEED),
			seed("Kwuarm", 56, ItemID.KWUARM_SEED),
			seed("Snapdragon", 62, ItemID.SNAPDRAGON_SEED),
			seed("Huasca", 65, ItemID.HUASCA_SEED),
			seed("Cadantine", 67, ItemID.CADANTINE_SEED),
			seed("Lantadyme", 73, ItemID.LANTADYME_SEED),
			seed("Dwarf weed", 79, ItemID.DWARF_WEED_SEED),
			seed("Torstol", 85, ItemID.TORSTOL_SEED));

		add(crops, PatchType.HOPS, 4,
			seed("Barley", 3, ItemID.BARLEY_SEED),
			seed("Hammerstone", 4, ItemID.HAMMERSTONE_HOP_SEED),
			seed("Asgarnian", 8, ItemID.ASGARNIAN_HOP_SEED),
			seed("Jute", 13, ItemID.JUTE_SEED),
			seed("Yanillian", 16, ItemID.YANILLIAN_HOP_SEED),
			seed("Flax", 18, ItemID.FLAX_SEED, 3),
			seed("Krandorian", 21, ItemID.KRANDORIAN_HOP_SEED),
			seed("Wildblood", 28, ItemID.WILDBLOOD_HOP_SEED),
			seed("Hemp", 37, ItemID.HEMP_SEED, 3),
			seed("Cotton", 71, ItemID.COTTON_SEED, 3));

		add(crops, PatchType.BUSH, 1,
			seed("Redberry", 10, ItemID.REDBERRY_BUSH_SEED),
			seed("Cadavaberry", 22, ItemID.CADAVABERRY_BUSH_SEED),
			seed("Dwellberry", 36, ItemID.DWELLBERRY_BUSH_SEED),
			seed("Jangerberry", 48, ItemID.JANGERBERRY_BUSH_SEED),
			seed("Whiteberry", 59, ItemID.WHITEBERRY_BUSH_SEED),
			seed("Poison ivy", 70, ItemID.POISONIVY_BUSH_SEED));

		add(crops, PatchType.TREE, 1,
			crop("Oak sapling", 15, ItemID.PLANTPOT_OAK_SAPLING),
			crop("Willow sapling", 30, ItemID.PLANTPOT_WILLOW_SAPLING),
			crop("Maple sapling", 45, ItemID.PLANTPOT_MAPLE_SAPLING),
			crop("Yew sapling", 60, ItemID.PLANTPOT_YEW_SAPLING),
			crop("Magic sapling", 75, ItemID.PLANTPOT_MAGIC_TREE_SAPLING));

		add(crops, PatchType.FRUIT_TREE, 1,
			crop("Apple sapling", 27, ItemID.PLANTPOT_APPLE_SAPLING),
			crop("Banana sapling", 33, ItemID.PLANTPOT_BANANA_SAPLING),
			crop("Orange sapling", 39, ItemID.PLANTPOT_ORANGE_SAPLING),
			crop("Curry sapling", 42, ItemID.PLANTPOT_CURRY_SAPLING),
			crop("Pineapple sapling", 51, ItemID.PLANTPOT_PINEAPPLE_SAPLING),
			crop("Papaya sapling", 57, ItemID.PLANTPOT_PAPAYA_SAPLING),
			crop("Palm sapling", 68, ItemID.PLANTPOT_PALM_SAPLING),
			crop("Dragonfruit sapling", 81, ItemID.PLANTPOT_DRAGONFRUIT_SAPLING));

		add(crops, PatchType.HARDWOOD_TREE, 1,
			crop("Teak sapling", 35, ItemID.PLANTPOT_TEAK_SAPLING),
			crop("Mahogany sapling", 55, ItemID.PLANTPOT_MAHOGANY_SAPLING),
			crop("Camphor sapling", 66, ItemID.PLANTPOT_CAMPHOR_SAPLING),
			crop("Ironwood sapling", 80, ItemID.PLANTPOT_IRONWOOD_SAPLING),
			crop("Rosewood sapling", 92, ItemID.PLANTPOT_ROSEWOOD_SAPLING));

		add(crops, PatchType.CACTUS, 1,
			seed("Cactus", 55, ItemID.CACTUS_SEED),
			seed("Potato cactus", 64, ItemID.POTATO_CACTUS_SEED));
		add(crops, PatchType.MUSHROOM, 1, crop("Mushroom", "Mushroom spore", 53, ItemID.MUSHROOM_SEED));
		add(crops, PatchType.BELLADONNA, 1, seed("Belladonna", 63, ItemID.BELLADONNA_SEED));
		add(crops, PatchType.CALQUAT, 1, crop("Calquat sapling", 72, ItemID.PLANTPOT_CALQUAT_SAPLING));
		add(crops, PatchType.SPIRIT_TREE, 1, crop("Spirit sapling", 83, ItemID.PLANTPOT_SPIRIT_TREE_SAPLING));
		add(crops, PatchType.SEAWEED, 1, crop("Seaweed", "Seaweed spore", 23, ItemID.SEAWEED_SEED));
		add(crops, PatchType.GRAPEVINE, 1, seed("Grape", 36, ItemID.GRAPE_SEED));
		add(crops, PatchType.CELASTRUS, 1, crop("Celastrus sapling", 85, ItemID.PLANTPOT_CELASTRUS_TREE_SAPLING));
		add(crops, PatchType.REDWOOD, 1, crop("Redwood sapling", 90, ItemID.PLANTPOT_REDWOOD_TREE_SAPLING));
		add(crops, PatchType.HESPORI, 1, seed("Hespori", 65, ItemID.HESPORI_SEED));
		add(crops, PatchType.ANIMA, 1,
			seed("Attas", 76, ItemID.ATTAS_SEED),
			seed("Iasor", 76, ItemID.IASOR_SEED),
			seed("Kronos", 76, ItemID.KRONOS_SEED));
		add(crops, PatchType.CRYSTAL_TREE, 1, crop("Crystal sapling", 74, ItemID.PLANTPOT_CRYSTAL_TREE_SAPLING));
		add(crops, PatchType.CORAL, 1,
			crop("Elkhorn fragment", "Elkhorn frag", 28, ItemID.CORAL_ELKHORN_FRAG),
			crop("Pillar fragment", "Pillar frag", 52, ItemID.CORAL_PILLAR_FRAG),
			crop("Umbral fragment", "Umbral frag", 77, ItemID.CORAL_UMBRAL_FRAG));

		return Collections.unmodifiableMap(crops);
	}

	private static CropSpec crop(String name, int level, int itemId)
	{
		return new CropSpec(name, name, level, itemId, -1);
	}

	private static CropSpec crop(String cropName, String itemName, int level, int itemId)
	{
		return new CropSpec(cropName, itemName, level, itemId, -1);
	}

	private static CropSpec seed(String cropName, int level, int itemId)
	{
		return seed(cropName, level, itemId, -1);
	}

	private static CropSpec seed(String cropName, int level, int itemId, int quantity)
	{
		return new CropSpec(cropName, cropName + " seed", level, itemId, quantity);
	}

	private static void add(Map<PatchType, List<Crop>> crops, PatchType patchType, int quantity, CropSpec... specs)
	{
		List<Crop> patchCrops = new ArrayList<>();
		for (CropSpec spec : specs)
		{
			patchCrops.add(new Crop(patchType, spec.cropName, spec.itemName, spec.level, spec.itemId,
				spec.quantity > 0 ? spec.quantity : quantity));
		}
		crops.put(patchType, Collections.unmodifiableList(patchCrops));
	}

	private static final class CropSpec
	{
		private final String cropName;
		private final String itemName;
		private final int level;
		private final int itemId;
		private final int quantity;

		private CropSpec(String cropName, String itemName, int level, int itemId, int quantity)
		{
			this.cropName = cropName;
			this.itemName = itemName;
			this.level = level;
			this.itemId = itemId;
			this.quantity = quantity;
		}
	}
}
