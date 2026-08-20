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
	private final FarmingContractManager contractManager;
	private final Provider<PatchTimerManager> timerManagerProvider;
	private final FarmRunFilterState runFilterState;
	private final Map<Integer, Integer> bankItems = new HashMap<>();
	private final Map<Integer, Integer> inventoryItems = new HashMap<>();
	private final Map<Integer, Integer> equipmentItems = new HashMap<>();
	private final Map<Integer, Integer> seedVaultItems = new HashMap<>();
	private final Map<PatchType, SeedRunBaseline> seedRunBaselines = new EnumMap<>(PatchType.class);
	private Set<Integer> cachedPaymentItemIds = Collections.emptySet();
	private int cachedPaymentSelectionHash;
	private int cachedPaymentFarmingLevel;
	private long cachedPaymentAtMillis;
	private boolean bankScanned;
	private boolean seedVaultScanned;

	@Inject
	private FarmingLoadout(FarmingPatchAdvisorPlugin plugin, FarmingPatchAdvisorConfig config,
		FarmingContractManager contractManager, Provider<PatchTimerManager> timerManagerProvider,
		FarmRunFilterState runFilterState)
	{
		this.plugin = plugin;
		this.config = config;
		this.contractManager = contractManager;
		this.timerManagerProvider = timerManagerProvider;
		this.runFilterState = runFilterState;
	}

	synchronized void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.BANK.getId())
		{
			copyItems(event.getItemContainer().getItems(), bankItems);
			bankScanned = true;
		}
		else if (event.getContainerId() == InventoryID.INVENTORY.getId())
		{
			copyItems(event.getItemContainer().getItems(), inventoryItems);
		}
		else if (event.getContainerId() == InventoryID.EQUIPMENT.getId())
		{
			copyItems(event.getItemContainer().getItems(), equipmentItems);
		}
		else if (event.getContainerId() == InventoryID.SEED_VAULT.getId())
		{
			copyItems(event.getItemContainer().getItems(), seedVaultItems);
			seedVaultScanned = true;
		}
		cachedPaymentAtMillis = 0;
	}

	synchronized void markBankScanned()
	{
		bankScanned = true;
	}

	synchronized void markSeedVaultScanned()
	{
		seedVaultScanned = true;
	}

	synchronized boolean isBankScanned()
	{
		return bankScanned;
	}

	synchronized boolean isSeedVaultScanned()
	{
		return seedVaultScanned;
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
		addContractSeed(selected);
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
		addContractSeed(selected);
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
		equipmentItems.clear();
		seedVaultItems.clear();
		seedRunBaselines.clear();
		bankScanned = false;
		seedVaultScanned = false;
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
				items.add(new ChecklistItem(crop.getItemName(), owned,
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
			for (ToolRequirement tool : requiredTools())
			{
				if (carriedCount(tool.itemId) == 0)
				{
					items.add(new ChecklistItem(tool.name, count(tool.itemId), 1, false));
				}
			}
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

	/**
	 * Builds the active Farming Guild contract as a separate storage checklist. Contract items
	 * deliberately do not follow the farm-run dropdown: an accepted contract remains actionable
	 * regardless of which normal run is currently selected.
	 */
	synchronized List<ChecklistItem> contractChecklist(boolean includePayments)
	{
		FarmingContract contract = config.showFarmingContract() ? contractManager.getContract() : null;
		if (contract == null)
		{
			return Collections.emptyList();
		}

		Crop crop = contract.getCrop();
		List<ChecklistItem> items = new ArrayList<>();
		items.add(new ChecklistItem(crop.getItemName(), inventoryCount(crop.getItemId()),
			crop.getQuantity(), true));
		if (includePayments)
		{
			for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(crop))
			{
				items.add(new ChecklistItem("Payment: " + payment.getName(),
					inventoryCountIncludingVariations(payment.getItemId()), payment.getQuantity(), false));
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
		addContractItemIds(itemIds, includePayments);
		addRequiredItemIds(itemIds, includeCompost, includeTools);
		for (int remedyItemId : remedyItemIds())
		{
			if (carriedCountIncludingVariations(remedyItemId) == 0)
			{
				itemIds.add(remedyItemId);
			}
		}
		return itemIds;
	}

	synchronized Set<Integer> remedyItemIds()
	{
		Set<Integer> itemIds = new HashSet<>();
		boolean diseased = false;
		boolean secateurs = false;
		for (PatchTimer timer : timerManagerProvider.get().getTimers())
		{
			if (!runFilterState.includes(timer.getPatchType()) || !timer.isDiseased())
			{
				continue;
			}
			diseased = true;
			secateurs |= PatchRemedy.usesSecateurs(timer.getPatchType());
		}
		if (diseased)
		{
			itemIds.add(ItemID.PLANT_CURE);
		}
		if (secateurs)
		{
			itemIds.add(count(ItemID.FAIRY_ENCHANTED_SECATEURS) > 0
				? ItemID.FAIRY_ENCHANTED_SECATEURS : ItemID.SECATEURS);
		}
		return itemIds;
	}

	synchronized boolean isRemedyItem(int itemId)
	{
		return remedyItemIds().contains(itemId);
	}

	synchronized boolean isRequiredToolItem(int itemId)
	{
		for (ToolRequirement tool : requiredTools())
		{
			if (tool.itemId == itemId)
			{
				return true;
			}
		}
		return false;
	}

	synchronized int inventoryQuantity(int itemId)
	{
		return inventoryCountIncludingVariations(itemId);
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
		FarmingContract contract = config.showFarmingContract() ? contractManager.getContract() : null;
		if (contract != null && runFilterState.includes(contract.getCrop().getPatchType()))
		{
			for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(contract.getCrop()))
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
			if (timer.isDead() || !runFilterState.includes(timer.getPatchType()))
			{
				continue;
			}
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
			+ equipmentItems.getOrDefault(itemId, 0) + seedVaultItems.getOrDefault(itemId, 0);
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
			for (ToolRequirement tool : requiredTools())
			{
				if (carriedCount(tool.itemId) == 0)
				{
					itemIds.add(tool.itemId);
				}
			}
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

	private void addContractSeed(Map<Integer, Crop> selected)
	{
		FarmingContract contract = config.showFarmingContract() ? contractManager.getContract() : null;
		if (contract != null && runFilterState.includes(contract.getCrop().getPatchType()))
		{
			selected.put(contract.getCrop().getItemId(), contract.getCrop());
		}
	}

	private void addContractItemIds(Set<Integer> itemIds, boolean includePayments)
	{
		FarmingContract contract = config.showFarmingContract() ? contractManager.getContract() : null;
		if (contract == null)
		{
			return;
		}
		itemIds.add(contract.getCrop().getItemId());
		if (includePayments)
		{
			for (ProtectionPayment payment : ProtectionPaymentCatalog.forCrop(contract.getCrop()))
			{
				itemIds.add(payment.getItemId());
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

	private List<ToolRequirement> requiredTools()
	{
		Map<PatchType, Integer> patchCounts = buildPatchCounts();
		List<ToolRequirement> tools = new ArrayList<>();
		if (patchCounts.isEmpty())
		{
			return tools;
		}
		tools.add(new ToolRequirement("Rake", ItemID.RAKE));
		tools.add(new ToolRequirement("Spade", ItemID.SPADE));
		if (patchCounts.keySet().stream().anyMatch(PatchType::usesDibber))
		{
			tools.add(new ToolRequirement("Seed dibber", ItemID.DIBBER));
		}
		if (patchCounts.keySet().stream().anyMatch(FarmingLoadout::isYieldBoostedByMagicSecateurs)
			&& count(ItemID.FAIRY_ENCHANTED_SECATEURS) > 0)
		{
			tools.add(new ToolRequirement("Magic secateurs", ItemID.FAIRY_ENCHANTED_SECATEURS));
		}
		if (patchCounts.keySet().stream().anyMatch(PatchType::canBeWatered))
		{
			int wateringCan = bestOwnedWateringCan();
			if (wateringCan >= 0)
			{
				tools.add(new ToolRequirement(wateringCanName(wateringCan), wateringCan));
			}
		}
		return tools;
	}

	private int bestOwnedWateringCan()
	{
		int[] wateringCans = {
			ItemID.ZEAH_WATERINGCAN, ItemID.WATERING_CAN_8, ItemID.WATERING_CAN_7,
			ItemID.WATERING_CAN_6, ItemID.WATERING_CAN_5, ItemID.WATERING_CAN_4,
			ItemID.WATERING_CAN_3, ItemID.WATERING_CAN_2, ItemID.WATERING_CAN_1
		};
		for (int itemId : wateringCans)
		{
			if (count(itemId) > 0)
			{
				return itemId;
			}
		}
		return -1;
	}

	private static String wateringCanName(int itemId)
	{
		switch (itemId)
		{
			case ItemID.ZEAH_WATERINGCAN: return "Gricoller's can";
			case ItemID.WATERING_CAN_8: return "Watering can(8)";
			case ItemID.WATERING_CAN_7: return "Watering can(7)";
			case ItemID.WATERING_CAN_6: return "Watering can(6)";
			case ItemID.WATERING_CAN_5: return "Watering can(5)";
			case ItemID.WATERING_CAN_4: return "Watering can(4)";
			case ItemID.WATERING_CAN_3: return "Watering can(3)";
			case ItemID.WATERING_CAN_2: return "Watering can(2)";
			case ItemID.WATERING_CAN_1: return "Watering can(1)";
			default: throw new IllegalArgumentException("Unknown watering can item " + itemId);
		}
	}

	static boolean isYieldBoostedByMagicSecateurs(PatchType patchType)
	{
		return patchType == PatchType.ALLOTMENT || patchType == PatchType.HERB
			|| patchType == PatchType.GRAPEVINE || patchType == PatchType.HOPS;
	}

	private int carriedCount(int itemId)
	{
		return inventoryItems.getOrDefault(itemId, 0) + equipmentItems.getOrDefault(itemId, 0);
	}

	private int carriedCountIncludingVariations(int itemId)
	{
		int total = 0;
		for (int variationId : ItemVariationMapping.getVariations(itemId))
		{
			total += carriedCount(variationId);
		}
		return total;
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
			if (runFilterState.includes(patch.getPatchType()))
			{
				counts.merge(patch.getPatchType(), patch.getPatchCount(), Integer::sum);
			}
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

	private static final class ToolRequirement
	{
		private final String name;
		private final int itemId;

		private ToolRequirement(String name, int itemId)
		{
			this.name = name;
			this.itemId = itemId;
		}
	}
}
