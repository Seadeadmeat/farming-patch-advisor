package com.farmingpatchadvisor;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("farming-patch-advisor")
public interface FarmingPatchAdvisorConfig extends Config
{
	@ConfigSection(name = "Crop overrides", description = "Override automatic seed recommendations by patch type", position = 9, closedByDefault = true)
	String cropOverridesSection = "cropOverridesSection";

	@ConfigSection(name = "Allotment, flower & herb run", description = "Patch types used on the frequent crop run", position = 10)
	String cropRunSection = "cropRunSection";

	@ConfigSection(name = "Tree run", description = "Regular tree patches", position = 11)
	String treeRunSection = "treeRunSection";

	@ConfigSection(name = "Fruit tree & calquat run", description = "Fruit-tree and calquat patches", position = 12)
	String fruitTreeRunSection = "fruitTreeRunSection";

	@ConfigSection(name = "Hardwood tree run", description = "Hardwood-tree patches", position = 13)
	String hardwoodRunSection = "hardwoodRunSection";

	@ConfigSection(name = "Hops run", description = "Hops patches", position = 14)
	String hopsRunSection = "hopsRunSection";

	@ConfigSection(name = "Bush run", description = "Bush patches", position = 15)
	String bushRunSection = "bushRunSection";

	@ConfigSection(name = "Cactus run", description = "Cactus patches", position = 16)
	String cactusRunSection = "cactusRunSection";

	@ConfigSection(name = "Specialty patch run", description = "Special-purpose and unique farming patches", position = 17)
	String specialtyRunSection = "specialtyRunSection";

	@ConfigItem(
		keyName = "showPatchLabels",
		name = "Show patch labels",
		description = "Show the highest-level unlocked crop over recognized farming patches",
		position = 0
	)
	default boolean showPatchLabels()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightBank",
		name = "Highlight in bank",
		description = "Highlight recommended seeds, saplings, and required farming items in the bank",
		position = 1
	)
	default boolean highlightBank()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightInventory",
		name = "Highlight in inventory",
		description = "Also highlight recommended seeds, saplings, and required farming items in the inventory",
		position = 2
	)
	default boolean highlightInventory()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightSeedVault",
		name = "Highlight in seed vault",
		description = "Highlight the highest-level usable seed or sapling in each seed-vault category",
		position = 3
	)
	default boolean highlightSeedVault()
	{
		return true;
	}

	@ConfigItem(
		keyName = "seedSelectionMode",
		name = "Seed recommendation",
		description = "Choose the highest-level seed you currently own or the highest-level seed your Farming level allows",
		position = 4
	)
	default SeedSelectionMode seedSelectionMode()
	{
		return SeedSelectionMode.HIGHEST_AVAILABLE;
	}

	@ConfigItem(keyName = "allotmentOverride", name = "Allotment", description = "Crop to recommend for allotment patches", position = 0, section = cropOverridesSection)
	default CropOverrides.Allotment allotmentOverride() { return CropOverrides.Allotment.AUTOMATIC; }
	@ConfigItem(keyName = "flowerOverride", name = "Flower", description = "Crop to recommend for flower patches", position = 1, section = cropOverridesSection)
	default CropOverrides.Flower flowerOverride() { return CropOverrides.Flower.AUTOMATIC; }
	@ConfigItem(keyName = "herbOverride", name = "Herb", description = "Crop to recommend for herb patches", position = 2, section = cropOverridesSection)
	default CropOverrides.Herb herbOverride() { return CropOverrides.Herb.AUTOMATIC; }
	@ConfigItem(keyName = "hopsOverride", name = "Hops", description = "Crop to recommend for hops patches", position = 3, section = cropOverridesSection)
	default CropOverrides.Hops hopsOverride() { return CropOverrides.Hops.AUTOMATIC; }
	@ConfigItem(keyName = "bushOverride", name = "Bush", description = "Crop to recommend for bush patches", position = 4, section = cropOverridesSection)
	default CropOverrides.Bush bushOverride() { return CropOverrides.Bush.AUTOMATIC; }
	@ConfigItem(keyName = "treeOverride", name = "Tree", description = "Crop to recommend for tree patches", position = 5, section = cropOverridesSection)
	default CropOverrides.Tree treeOverride() { return CropOverrides.Tree.AUTOMATIC; }
	@ConfigItem(keyName = "fruitTreeOverride", name = "Fruit tree", description = "Crop to recommend for fruit-tree patches", position = 6, section = cropOverridesSection)
	default CropOverrides.FruitTree fruitTreeOverride() { return CropOverrides.FruitTree.AUTOMATIC; }
	@ConfigItem(keyName = "hardwoodTreeOverride", name = "Hardwood tree", description = "Crop to recommend for hardwood-tree patches", position = 7, section = cropOverridesSection)
	default CropOverrides.HardwoodTree hardwoodTreeOverride() { return CropOverrides.HardwoodTree.AUTOMATIC; }
	@ConfigItem(keyName = "cactusOverride", name = "Cactus", description = "Crop to recommend for cactus patches", position = 8, section = cropOverridesSection)
	default CropOverrides.Cactus cactusOverride() { return CropOverrides.Cactus.AUTOMATIC; }
	@ConfigItem(keyName = "mushroomOverride", name = "Mushroom", description = "Crop to recommend for mushroom patches", position = 9, section = cropOverridesSection)
	default CropOverrides.Mushroom mushroomOverride() { return CropOverrides.Mushroom.AUTOMATIC; }
	@ConfigItem(keyName = "belladonnaOverride", name = "Belladonna", description = "Crop to recommend for belladonna patches", position = 10, section = cropOverridesSection)
	default CropOverrides.Belladonna belladonnaOverride() { return CropOverrides.Belladonna.AUTOMATIC; }
	@ConfigItem(keyName = "calquatOverride", name = "Calquat", description = "Crop to recommend for calquat patches", position = 11, section = cropOverridesSection)
	default CropOverrides.Calquat calquatOverride() { return CropOverrides.Calquat.AUTOMATIC; }
	@ConfigItem(keyName = "spiritTreeOverride", name = "Spirit tree", description = "Crop to recommend for spirit-tree patches", position = 12, section = cropOverridesSection)
	default CropOverrides.SpiritTree spiritTreeOverride() { return CropOverrides.SpiritTree.AUTOMATIC; }
	@ConfigItem(keyName = "seaweedOverride", name = "Seaweed", description = "Crop to recommend for seaweed patches", position = 13, section = cropOverridesSection)
	default CropOverrides.Seaweed seaweedOverride() { return CropOverrides.Seaweed.AUTOMATIC; }
	@ConfigItem(keyName = "grapevineOverride", name = "Grapevine", description = "Crop to recommend for grapevine patches", position = 14, section = cropOverridesSection)
	default CropOverrides.Grapevine grapevineOverride() { return CropOverrides.Grapevine.AUTOMATIC; }
	@ConfigItem(keyName = "celastrusOverride", name = "Celastrus", description = "Crop to recommend for celastrus patches", position = 15, section = cropOverridesSection)
	default CropOverrides.Celastrus celastrusOverride() { return CropOverrides.Celastrus.AUTOMATIC; }
	@ConfigItem(keyName = "redwoodOverride", name = "Redwood", description = "Crop to recommend for redwood patches", position = 16, section = cropOverridesSection)
	default CropOverrides.Redwood redwoodOverride() { return CropOverrides.Redwood.AUTOMATIC; }
	@ConfigItem(keyName = "hesporiOverride", name = "Hespori", description = "Crop to recommend for Hespori patches", position = 17, section = cropOverridesSection)
	default CropOverrides.Hespori hesporiOverride() { return CropOverrides.Hespori.AUTOMATIC; }
	@ConfigItem(keyName = "crystalTreeOverride", name = "Crystal tree", description = "Crop to recommend for crystal-tree patches", position = 18, section = cropOverridesSection)
	default CropOverrides.CrystalTree crystalTreeOverride() { return CropOverrides.CrystalTree.AUTOMATIC; }
	@ConfigItem(keyName = "coralOverride", name = "Coral", description = "Crop to recommend for coral patches", position = 19, section = cropOverridesSection)
	default CropOverrides.Coral coralOverride() { return CropOverrides.Coral.AUTOMATIC; }

	@ConfigItem(
		keyName = "includeCompost",
		name = "Include ultracompost",
		description = "Treat ultracompost and a charged bottomless compost bucket as part of the required item kit",
		position = 5
	)
	default boolean includeCompost()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTimerOverlay",
		name = "Show timer overlay",
		description = "Show persistent farming patch countdowns in a movable overlay",
		position = 6
	)
	default boolean showTimerOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showChecklistOverlay",
		name = "Show run checklist",
		description = "Show a movable checklist of seeds, saplings, and tools required for the full farm run",
		position = 7
	)
	default boolean showChecklistOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "checklistTools",
		name = "Checklist tools",
		description = "Include rake, seed dibber, and spade in the checklist",
		position = 8
	)
	default boolean checklistTools()
	{
		return true;
	}

	@ConfigItem(
		keyName = "checklistPayments",
		name = "Checklist payments",
		description = "Include gardener protection payments for the selected crops",
		position = 9
	)
	default boolean checklistPayments()
	{
		return true;
	}

	@ConfigItem(keyName = "checklistAllotment", name = "Include allotments",
		description = "Include allotment seeds", position = 0, section = cropRunSection)
	default boolean checklistAllotment() { return true; }

	@ConfigItem(keyName = "checklistFlower", name = "Include flowers",
		description = "Include flower seeds", position = 1, section = cropRunSection)
	default boolean checklistFlower() { return true; }

	@ConfigItem(keyName = "checklistHerb", name = "Include herbs",
		description = "Include herb seeds", position = 2, section = cropRunSection)
	default boolean checklistHerb() { return true; }

	@ConfigItem(keyName = "checklistHops", name = "Include hops",
		description = "Include hops seeds", position = 0, section = hopsRunSection)
	default boolean checklistHops() { return true; }

	@ConfigItem(keyName = "checklistBush", name = "Include bushes",
		description = "Include bush seeds", position = 0, section = bushRunSection)
	default boolean checklistBush() { return true; }

	@ConfigItem(keyName = "checklistTree", name = "Include trees",
		description = "Include tree saplings", position = 0, section = treeRunSection)
	default boolean checklistTree() { return true; }

	@ConfigItem(keyName = "checklistFruitTree", name = "Include fruit trees",
		description = "Include fruit-tree saplings", position = 0, section = fruitTreeRunSection)
	default boolean checklistFruitTree() { return true; }

	@ConfigItem(keyName = "checklistHardwoodTree", name = "Include hardwood trees",
		description = "Include hardwood-tree saplings", position = 0, section = hardwoodRunSection)
	default boolean checklistHardwoodTree() { return true; }

	@ConfigItem(keyName = "checklistCactus", name = "Include cactus",
		description = "Include cactus seeds", position = 0, section = cactusRunSection)
	default boolean checklistCactus() { return true; }

	@ConfigItem(keyName = "checklistMushroom", name = "Include mushroom",
		description = "Include mushroom spores", position = 0, section = specialtyRunSection)
	default boolean checklistMushroom() { return true; }

	@ConfigItem(keyName = "checklistBelladonna", name = "Include belladonna",
		description = "Include belladonna seeds", position = 1, section = specialtyRunSection)
	default boolean checklistBelladonna() { return true; }

	@ConfigItem(keyName = "checklistCalquat", name = "Include calquat",
		description = "Include calquat saplings", position = 1, section = fruitTreeRunSection)
	default boolean checklistCalquat() { return true; }

	@ConfigItem(keyName = "checklistSpiritTree", name = "Include spirit trees",
		description = "Include spirit-tree saplings", position = 2, section = specialtyRunSection)
	default boolean checklistSpiritTree() { return true; }

	@ConfigItem(keyName = "checklistSeaweed", name = "Include seaweed",
		description = "Include seaweed spores", position = 3, section = specialtyRunSection)
	default boolean checklistSeaweed() { return true; }

	@ConfigItem(keyName = "checklistGrapevine", name = "Include grapevines",
		description = "Include grape seeds", position = 4, section = specialtyRunSection)
	default boolean checklistGrapevine() { return true; }

	@ConfigItem(keyName = "checklistCelastrus", name = "Include celastrus",
		description = "Include celastrus saplings", position = 5, section = specialtyRunSection)
	default boolean checklistCelastrus() { return true; }

	@ConfigItem(keyName = "checklistRedwood", name = "Include redwood",
		description = "Include redwood saplings", position = 6, section = specialtyRunSection)
	default boolean checklistRedwood() { return true; }

	@ConfigItem(keyName = "checklistHespori", name = "Include Hespori",
		description = "Include Hespori seeds", position = 7, section = specialtyRunSection)
	default boolean checklistHespori() { return true; }

	@ConfigItem(keyName = "checklistCrystalTree", name = "Include crystal trees",
		description = "Include crystal-tree saplings", position = 8, section = specialtyRunSection)
	default boolean checklistCrystalTree() { return true; }

	@ConfigItem(keyName = "checklistCoral", name = "Include coral",
		description = "Include coral fragments", position = 9, section = specialtyRunSection)
	default boolean checklistCoral() { return true; }

	@Alpha
	@ConfigItem(
		keyName = "seedColor",
		name = "Seed color",
		description = "Color used for recommended seeds and saplings",
		position = 29
	)
	default Color seedColor()
	{
		return new Color(0, 255, 80, 220);
	}

	@Alpha
	@ConfigItem(
		keyName = "requiredItemColor",
		name = "Required item color",
		description = "Color used for rakes, dibbers, spades, and compost",
		position = 30
	)
	default Color requiredItemColor()
	{
		return new Color(50, 180, 255, 220);
	}
}
