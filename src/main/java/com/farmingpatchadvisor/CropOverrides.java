package com.farmingpatchadvisor;

final class CropOverrides
{
	interface Choice
	{
		String cropName();
	}

	private CropOverrides()
	{
	}

	static Crop selected(FarmingPatchAdvisorConfig config, PatchType type)
	{
		Choice choice;
		switch (type)
		{
			case ALLOTMENT: choice = config.allotmentOverride(); break;
			case FLOWER: choice = config.flowerOverride(); break;
			case HERB: choice = config.herbOverride(); break;
			case HOPS: choice = config.hopsOverride(); break;
			case BUSH: choice = config.bushOverride(); break;
			case TREE: choice = config.treeOverride(); break;
			case FRUIT_TREE: choice = config.fruitTreeOverride(); break;
			case HARDWOOD_TREE: choice = config.hardwoodTreeOverride(); break;
			case CACTUS: choice = config.cactusOverride(); break;
			case MUSHROOM: choice = config.mushroomOverride(); break;
			case BELLADONNA: choice = config.belladonnaOverride(); break;
			case CALQUAT: choice = config.calquatOverride(); break;
			case SPIRIT_TREE: choice = config.spiritTreeOverride(); break;
			case SEAWEED: choice = config.seaweedOverride(); break;
			case GRAPEVINE: choice = config.grapevineOverride(); break;
			case CELASTRUS: choice = config.celastrusOverride(); break;
			case REDWOOD: choice = config.redwoodOverride(); break;
			case HESPORI: choice = config.hesporiOverride(); break;
			case CRYSTAL_TREE: choice = config.crystalTreeOverride(); break;
			case CORAL: choice = config.coralOverride(); break;
			default: return null;
		}
		return choice.cropName() == null ? null : CropCatalog.findByName(type, choice.cropName());
	}

	public enum Allotment implements Choice { AUTOMATIC(null), POTATO("Potato"), ONION("Onion"), CABBAGE("Cabbage"), TOMATO("Tomato"), SWEETCORN("Sweetcorn"), STRAWBERRY("Strawberry"), WATERMELON("Watermelon"), SNAPE_GRASS("Snape grass"); private final String n; Allotment(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Flower implements Choice { AUTOMATIC(null), MARIGOLD("Marigold"), ROSEMARY("Rosemary"), NASTURTIUM("Nasturtium"), WOAD("Woad"), LIMPWURT("Limpwurt"), WHITE_LILY("White lily"); private final String n; Flower(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Herb implements Choice { AUTOMATIC(null), GUAM("Guam"), MARRENTILL("Marrentill"), TARROMIN("Tarromin"), HARRALANDER("Harralander"), RANARR("Ranarr"), TOADFLAX("Toadflax"), IRIT("Irit"), AVANTOE("Avantoe"), KWUARM("Kwuarm"), SNAPDRAGON("Snapdragon"), HUASCA("Huasca"), CADANTINE("Cadantine"), LANTADYME("Lantadyme"), DWARF_WEED("Dwarf weed"), TORSTOL("Torstol"); private final String n; Herb(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Hops implements Choice { AUTOMATIC(null), BARLEY("Barley"), HAMMERSTONE("Hammerstone"), ASGARNIAN("Asgarnian"), JUTE("Jute"), YANILLIAN("Yanillian"), KRANDORIAN("Krandorian"), WILDBLOOD("Wildblood"), HEMP("Hemp"), COTTON("Cotton"); private final String n; Hops(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Bush implements Choice { AUTOMATIC(null), REDBERRY("Redberry"), CADAVABERRY("Cadavaberry"), DWELLBERRY("Dwellberry"), JANGERBERRY("Jangerberry"), WHITEBERRY("Whiteberry"), POISON_IVY("Poison ivy"); private final String n; Bush(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Tree implements Choice { AUTOMATIC(null), OAK("Oak sapling"), WILLOW("Willow sapling"), MAPLE("Maple sapling"), YEW("Yew sapling"), MAGIC("Magic sapling"); private final String n; Tree(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum FruitTree implements Choice { AUTOMATIC(null), APPLE("Apple sapling"), BANANA("Banana sapling"), ORANGE("Orange sapling"), CURRY("Curry sapling"), PINEAPPLE("Pineapple sapling"), PAPAYA("Papaya sapling"), PALM("Palm sapling"), DRAGONFRUIT("Dragonfruit sapling"); private final String n; FruitTree(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum HardwoodTree implements Choice { AUTOMATIC(null), TEAK("Teak sapling"), MAHOGANY("Mahogany sapling"), CAMPHOR("Camphor sapling"), IRONWOOD("Ironwood sapling"), ROSEWOOD("Rosewood sapling"); private final String n; HardwoodTree(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Cactus implements Choice { AUTOMATIC(null), CACTUS("Cactus"), POTATO_CACTUS("Potato cactus"); private final String n; Cactus(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Mushroom implements Choice { AUTOMATIC(null), MUSHROOM("Mushroom"); private final String n; Mushroom(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Belladonna implements Choice { AUTOMATIC(null), BELLADONNA("Belladonna"); private final String n; Belladonna(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Calquat implements Choice { AUTOMATIC(null), CALQUAT("Calquat sapling"); private final String n; Calquat(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum SpiritTree implements Choice { AUTOMATIC(null), SPIRIT_TREE("Spirit sapling"); private final String n; SpiritTree(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Seaweed implements Choice { AUTOMATIC(null), SEAWEED("Seaweed"); private final String n; Seaweed(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Grapevine implements Choice { AUTOMATIC(null), GRAPE("Grape"); private final String n; Grapevine(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Celastrus implements Choice { AUTOMATIC(null), CELASTRUS("Celastrus sapling"); private final String n; Celastrus(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Redwood implements Choice { AUTOMATIC(null), REDWOOD("Redwood sapling"); private final String n; Redwood(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Hespori implements Choice { AUTOMATIC(null), HESPORI("Hespori"); private final String n; Hespori(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum CrystalTree implements Choice { AUTOMATIC(null), CRYSTAL_TREE("Crystal sapling"); private final String n; CrystalTree(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
	public enum Coral implements Choice { AUTOMATIC(null), ELKHORN("Elkhorn fragment"), PILLAR("Pillar fragment"), UMBRAL("Umbral fragment"); private final String n; Coral(String n){this.n=n;} public String cropName(){return n;} public String toString(){return n == null ? "Automatic" : n;} }
}
