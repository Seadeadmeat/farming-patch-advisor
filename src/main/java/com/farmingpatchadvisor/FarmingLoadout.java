package com.farmingpatchadvisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemVariationMapping;

@Singleton
final class FarmingLoadout
{
	private final FarmingPatchAdvisorPlugin plugin;
	private final FarmingPatchAdvisorConfig config;
	private final Provider<PatchTimerManager> timerManagerProvider;
	private final Map<Integer, Integer> bankItems = new HashMap<>();
	private final Map<Integer, Integer> inventoryItems = new HashMap<>();
	private final Map<Integer, Integer> seedVaultItems = new HashMap<>();
	private final Map<PatchType, SeedRunBaseline> seedRunBaselines = new EnumMap<>(PatchType.class);
	private Set<Integer> cachedPaymentItemIds = Collections.emptySet();
	private int cachedPaymentSelectionHash;
	private int cachedPaymentFarmingLevel;
	private long cachedPaymentAtMillis;

	@Inject
	private FarmingLoadout(FarmingPatchAdvisorPlugin plugin, FarmingPatchAdvisorConfig config,
		Provider<PatchTimerManager> timerManagerProvider)
	{
		this.plugin = plugin;
		this.config = config;
		this.timerManagerProvider = timerManagerProvider;
	}

	synchronized void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.BANK.getId())
		{
			copyItems(event.getItemContainer().getItems(), bankItems);
		}
		else if (event.getContainerId() == InventoryID.INVENTORY.getId())
		{
			copyItems(event.getItemContainer().getItems(), inventoryItems);
		}
		else if (event.getContainerId() == InventoryID.SEED_VAULT.getId())
		{
			copyItems(event.getItemContainer().getItems(), seedVaultItems);
		}
		cachedPaymentAtMillis = 0;
	}

	synchronized Map<Integer, Crop> bestAvailableByItemId()
	{
		Map<Integer, Crop> selected = new HashMap<>();
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		for (PatchType patchType : PatchType.values())
		{
			if (patchCounts.getOrDefault(patchType, 0) == 0)
			{
				continue;
			}
			Crop crop = selectedOutsideSeedVault(patchType);
			if (crop != null)
			{
				selected.put(crop.getItemId(), crop);
			}
		}
		return selected;
	}

	synchronized Map<Integer, Crop> bestAvailableInSeedVaultByItemId()
	{
		Map<Integer, Crop> selected = new HashMap<>();
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		for (PatchType patchType : PatchType.values())
		{
			if (patchCounts.getOrDefault(patchType, 0) == 0)
			{
				continue;
			}
			Crop crop = CropOverrides.selected(config, patchType);
			if (crop == null)
			{
				crop = config.seedSelectionMode() == SeedSelectionMode.HIGHEST_LEVEL
					? CropCatalog.recommend(patchType, plugin.getFarmingLevel())
					: bestAvailable(patchType, seedVaultItems);
			}
			if (crop != null)
			{
				selected.put(crop.getItemId(), crop);
			}
		}
		return selected;
	}

	synchronized Crop recommendedCrop(PatchType patchType)
	{
		Crop override = CropOverrides.selected(config, patchType);
		if (override != null)
		{
			return override;
		}
		return config.seedSelectionMode() == SeedSelectionMode.HIGHEST_LEVEL
			? CropCatalog.recommend(patchType, plugin.getFarmingLevel())
			: bestAvailable(patchType);
	}

	synchronized void clearStorageSnapshots()
	{
		bankItems.clear();
		inventoryItems.clear();
		seedVaultItems.clear();
		seedRunBaselines.clear();
		cachedPaymentAtMillis = 0;
	}

	synchronized List<ChecklistItem> checklist(boolean includeCompost, boolean includeTools, boolean includePayments,
		Set<ChecklistPatch> includedPatches)
	{
		List<ChecklistItem> items = new ArrayList<>();
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		Map<Integer, PaymentTotal> payments = new LinkedHashMap<>();
		RunProgress progress = runProgress();
		for (PatchType patchType : PatchType.values())
		{
			if (!includes(includedPatches, patchType))
			{
				continue;
			}
			int patches = patchCounts.getOrDefault(patchType, 0);
			if (patches == 0)
			{
				continue;
			}
			int occupiedPatches = progress.occupiedPatches.getOrDefault(patchType, 0);
			Crop crop = bestChecklistCrop(patchType, progress, occupiedPatches);
			if (crop == null)
			{
				crop = CropCatalog.recommend(patchType, plugin.getFarmingLevel());
			}
			if (crop != null)
			{
				int consumedSeeds = progress.consumedSeedsByItemId.getOrDefault(crop.getItemId(), 0);
				stableOwnedSeedCount(patchType, crop, occupiedPatches, consumedSeeds);
				int owned = inventoryCount(crop.getItemId());
				items.add(new ChecklistItem(crop.getName(), owned,
					remainingSeedQuantity(patches, occupiedPatches, crop.getQuantity()), true));
				if (includePayments)
				{
					for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(crop))
					{
						payments.computeIfAbsent(payment.getItemId(), ignored ->
							new PaymentTotal(payment.getName(), payment.getItemId()))
							.add(patches * payment.getQuantity());
					}
				}
			}
		}
		for (PaymentTotal payment : payments.values())
		{
			items.add(new ChecklistItem("Payment: " + payment.name,
				inventoryCountIncludingVariations(payment.itemId), payment.quantity, false));
		}
		if (includeTools)
		{
			addToolIfMissing(items, "Rake", ItemID.RAKE);
			addToolIfMissing(items, "Seed dibber", ItemID.DIBBER);
			addToolIfMissing(items, "Spade", ItemID.SPADE);
		}
		if (includeCompost)
		{
			int bucketCount = count(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED);
			if (bucketCount > 0)
			{
				if (inventoryCount(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED) == 0)
				{
					items.add(new ChecklistItem("Bottomless compost bucket", 0, 1, false));
				}
			}
			else
			{
				int totalPatches = patchCounts.entrySet().stream()
					.filter(entry -> includes(includedPatches, entry.getKey()))
					.mapToInt(Map.Entry::getValue).sum();
				items.add(new ChecklistItem("Ultracompost", inventoryCount(ItemID.BUCKET_ULTRACOMPOST),
					totalPatches, false));
			}
		}
		return items;
	}

	synchronized Set<Integer> bankChecklistItemIds(boolean includeCompost, boolean includeTools, boolean includePayments,
		Set<ChecklistPatch> includedPatches)
	{
		Set<Integer> itemIds = new HashSet<>();
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		for (PatchType patchType : PatchType.values())
		{
			if (!includes(includedPatches, patchType) || patchCounts.getOrDefault(patchType, 0) == 0)
			{
				continue;
			}
			Crop crop = selectedOutsideSeedVault(patchType);
			if (crop == null)
			{
				crop = CropCatalog.recommend(patchType, plugin.getFarmingLevel());
			}
			if (crop != null)
			{
				itemIds.add(crop.getItemId());
				if (includePayments)
				{
					for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(crop))
					{
						itemIds.add(payment.getItemId());
					}
				}
			}
		}
		addRequiredItemIds(itemIds, includeCompost, includeTools);
		return itemIds;
	}

	synchronized Set<Integer> protectionPaymentItemIds(Set<ChecklistPatch> includedPatches)
	{
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		int selectionHash = 31 * (includedPatches == null ? 0 : includedPatches.hashCode()) + patchCounts.hashCode();
		int farmingLevel = plugin.getFarmingLevel();
		long now = System.currentTimeMillis();
		if (now - cachedPaymentAtMillis < 500L && selectionHash == cachedPaymentSelectionHash
			&& farmingLevel == cachedPaymentFarmingLevel)
		{
			return cachedPaymentItemIds;
		}

		Set<Integer> itemIds = new HashSet<>();
		RunProgress progress = runProgress();
		for (PatchType patchType : PatchType.values())
		{
			if (!includes(includedPatches, patchType) || patchCounts.getOrDefault(patchType, 0) == 0)
			{
				continue;
			}
			int occupiedPatches = progress.occupiedPatches.getOrDefault(patchType, 0);
			Crop crop = bestChecklistCrop(patchType, progress, occupiedPatches);
			if (crop == null)
			{
				crop = CropCatalog.recommend(patchType, plugin.getFarmingLevel());
			}
			for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(crop))
			{
				itemIds.add(payment.getItemId());
			}
		}
		cachedPaymentItemIds = Collections.unmodifiableSet(itemIds);
		cachedPaymentSelectionHash = selectionHash;
		cachedPaymentFarmingLevel = farmingLevel;
		cachedPaymentAtMillis = now;
		return cachedPaymentItemIds;
	}

	private Crop bestChecklistCrop(PatchType patchType, RunProgress progress, int occupiedPatches)
	{
		Crop override = CropOverrides.selected(config, patchType);
		if (override != null)
		{
			return override;
		}
		Crop activeCrop = progress.activeCrops.get(patchType);
		if (occupiedPatches > 0 && activeCrop != null)
		{
			return activeCrop;
		}
		SeedRunBaseline baseline = seedRunBaselines.get(patchType);
		if (occupiedPatches > 0 && baseline != null)
		{
			Crop baselineCrop = CropCatalog.findByItemId(baseline.cropItemId);
			if (baselineCrop != null)
			{
				return baselineCrop;
			}
		}
		if (config.seedSelectionMode() == SeedSelectionMode.HIGHEST_LEVEL)
		{
			return CropCatalog.recommend(patchType, plugin.getFarmingLevel());
		}
		Crop best = null;
		int farmingLevel = plugin.getFarmingLevel();
		for (Crop crop : CropCatalog.crops(patchType))
		{
			if (crop.getLevel() <= farmingLevel
				&& count(crop.getItemId()) + progress.consumedSeedsByItemId.getOrDefault(crop.getItemId(), 0) > 0
				&& (best == null || crop.getLevel() > best.getLevel()))
			{
				best = crop;
			}
		}
		return best;
	}

	private RunProgress runProgress()
	{
		RunProgress progress = new RunProgress();
		for (PatchTimer timer : timerManagerProvider.get().getTimers())
		{
			progress.occupiedPatches.merge(timer.getPatchType(), 1, Integer::sum);
			Crop activeCrop = progress.activeCrops.get(timer.getPatchType());
			if (activeCrop == null || timer.getCrop().getLevel() > activeCrop.getLevel())
			{
				progress.activeCrops.put(timer.getPatchType(), timer.getCrop());
			}
			if (timer.isPlantedTimer())
			{
				progress.consumedSeedsByItemId.merge(timer.getCrop().getItemId(),
					timer.getCrop().getQuantity(), Integer::sum);
			}
		}
		return progress;
	}

	private Crop bestAvailable(PatchType patchType)
	{
		Crop best = null;
		int farmingLevel = plugin.getFarmingLevel();
		for (Crop crop : CropCatalog.crops(patchType))
		{
			if (crop.getLevel() <= farmingLevel && count(crop.getItemId()) > 0
				&& (best == null || crop.getLevel() > best.getLevel()))
			{
				best = crop;
			}
		}
		return best;
	}

	private Crop bestAvailableOutsideSeedVault(PatchType patchType)
	{
		Crop best = null;
		int farmingLevel = plugin.getFarmingLevel();
		for (Crop crop : CropCatalog.crops(patchType))
		{
			int available = bankItems.getOrDefault(crop.getItemId(), 0)
				+ inventoryItems.getOrDefault(crop.getItemId(), 0);
			if (crop.getLevel() <= farmingLevel && available > 0
				&& (best == null || crop.getLevel() > best.getLevel()))
			{
				best = crop;
			}
		}
		return best;
	}

	private Crop bestAvailable(PatchType patchType, Map<Integer, Integer> source)
	{
		Crop best = null;
		int farmingLevel = plugin.getFarmingLevel();
		for (Crop crop : CropCatalog.crops(patchType))
		{
			if (crop.getLevel() <= farmingLevel && source.getOrDefault(crop.getItemId(), 0) > 0
				&& (best == null || crop.getLevel() > best.getLevel()))
			{
				best = crop;
			}
		}
		return best;
	}

	private Crop selectedOutsideSeedVault(PatchType patchType)
	{
		Crop override = CropOverrides.selected(config, patchType);
		if (override != null)
		{
			return override;
		}
		return config.seedSelectionMode() == SeedSelectionMode.HIGHEST_LEVEL
			? CropCatalog.recommend(patchType, plugin.getFarmingLevel())
			: bestAvailableOutsideSeedVault(patchType);
	}

	private int count(int itemId)
	{
		return bankItems.getOrDefault(itemId, 0) + inventoryItems.getOrDefault(itemId, 0)
			+ seedVaultItems.getOrDefault(itemId, 0);
	}

	private int inventoryCount(int itemId)
	{
		return inventoryItems.getOrDefault(itemId, 0);
	}

	private int inventoryCountIncludingVariations(int itemId)
	{
		int total = 0;
		for (int variationId : ItemVariationMapping.getVariations(itemId))
		{
			total += inventoryCount(variationId);
		}
		return total;
	}

	private void addToolIfMissing(List<ChecklistItem> items, String name, int itemId)
	{
		if (inventoryCount(itemId) == 0)
		{
			items.add(new ChecklistItem(name, count(itemId), 1, false));
		}
	}

	private int countIncludingVariations(int itemId)
	{
		int total = 0;
		for (int variationId : ItemVariationMapping.getVariations(itemId))
		{
			total += count(variationId);
		}
		return total;
	}

	private void addRequiredItemIds(Set<Integer> itemIds, boolean includeCompost, boolean includeTools)
	{
		if (includeTools)
		{
			addItemIdIfMissingFromInventory(itemIds, ItemID.RAKE);
			addItemIdIfMissingFromInventory(itemIds, ItemID.DIBBER);
			addItemIdIfMissingFromInventory(itemIds, ItemID.SPADE);
		}
		if (includeCompost)
		{
			if (bankItems.getOrDefault(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED, 0)
				+ inventoryItems.getOrDefault(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED, 0) > 0)
			{
				addItemIdIfMissingFromInventory(itemIds, ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED);
			}
			else
			{
				itemIds.add(ItemID.BUCKET_ULTRACOMPOST);
			}
		}
	}

	private void addItemIdIfMissingFromInventory(Set<Integer> itemIds, int itemId)
	{
		if (inventoryCount(itemId) == 0)
		{
			itemIds.add(itemId);
		}
	}

	private static boolean includes(Set<ChecklistPatch> includedPatches, PatchType patchType)
	{
		if (includedPatches == null)
		{
			return true;
		}
		for (ChecklistPatch patch : includedPatches)
		{
			if (patch.getPatchType() == patchType)
			{
				return true;
			}
		}
		return false;
	}

	private static void copyItems(Item[] items, Map<Integer, Integer> destination)
	{
		destination.clear();
		for (Item item : items)
		{
			if (item.getId() >= 0 && item.getQuantity() > 0)
			{
				destination.merge(item.getId(), item.getQuantity(), Integer::sum);
			}
		}
	}

	private int stableOwnedSeedCount(PatchType patchType, Crop crop, int occupiedPatches,
		int consumedByTrackedPlanting)
	{
		int currentlyOwned = count(crop.getItemId());
		int adjustedOwned = currentlyOwned + consumedByTrackedPlanting;
		SeedRunBaseline baseline = seedRunBaselines.get(patchType);
		if (baseline == null || baseline.cropItemId != crop.getItemId())
		{
			baseline = new SeedRunBaseline(crop.getItemId(), adjustedOwned);
			seedRunBaselines.put(patchType, baseline);
		}
		else
		{
			baseline.owned = stableOwnedSeedCount(baseline.owned, occupiedPatches > 0,
				occupiedPatches > 0 ? adjustedOwned : currentlyOwned);
		}
		return baseline.owned;
	}

	static int stableOwnedSeedCount(int previousBaseline, boolean runInProgress, int currentlyOwned)
	{
		return runInProgress ? Math.max(previousBaseline, currentlyOwned) : currentlyOwned;
	}

	static int remainingSeedQuantity(int patchCount, int occupiedPatchCount, int seedsPerPatch)
	{
		return Math.max(0, patchCount - occupiedPatchCount) * seedsPerPatch;
	}

	private Map<PatchType, Integer> buildPatchCounts()
	{
		Map<PatchType, Integer> counts = new EnumMap<>(PatchType.class);
		for (FarmRunPatch patch : FarmRunCatalog.patches(config))
		{
			counts.merge(patch.getPatchType(), patch.getPatchCount(), Integer::sum);
		}
		return counts;
	}

	static final class ChecklistItem
	{
		private final String name;
		private final int owned;
		private final int needed;
		private final boolean seed;

		private ChecklistItem(String name, int owned, int needed, boolean seed)
		{
			this.name = name;
			this.owned = owned;
			this.needed = needed;
			this.seed = seed;
		}

		String getName() { return name; }
		int getOwned() { return owned; }
		int getNeeded() { return needed; }
		boolean isSeed() { return seed; }
	}

	private static final class PaymentTotal
	{
		private final String name;
		private final int itemId;
		private int quantity;

		private PaymentTotal(String name, int itemId)
		{
			this.name = name;
			this.itemId = itemId;
		}

		private void add(int amount)
		{
			quantity += amount;
		}
	}

	private static final class RunProgress
	{
		private final Map<PatchType, Integer> occupiedPatches = new EnumMap<>(PatchType.class);
		private final Map<Integer, Integer> consumedSeedsByItemId = new HashMap<>();
		private final Map<PatchType, Crop> activeCrops = new EnumMap<>(PatchType.class);
	}

	private static final class SeedRunBaseline
	{
		private final int cropItemId;
		private int owned;

		private SeedRunBaseline(int cropItemId, int owned)
		{
			this.cropItemId = cropItemId;
			this.owned = owned;
		}
	}
}
