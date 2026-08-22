package com.farmingpatchadvisor;

import java.time.Duration;

final class CropGrowthTimes
{
	private CropGrowthTimes()
	{
	}

	static Duration forCrop(Crop crop)
	{
		int minutes;
		switch (crop.getName())
		{
			case "Potato":
			case "Onion":
			case "Cabbage":
			case "Tomato": minutes = 40; break;
			case "Sweetcorn":
			case "Strawberry": minutes = 50; break;
			case "Watermelon": minutes = 80; break;
			case "Snape grass": minutes = 70; break;

			case "Marigold":
			case "Rosemary":
			case "Nasturtium":
			case "Woad":
			case "Limpwurt":
			case "White lily": minutes = 20; break;

			case "Guam":
			case "Marrentill":
			case "Tarromin":
			case "Harralander":
			case "Ranarr":
			case "Toadflax":
			case "Irit":
			case "Avantoe":
			case "Kwuarm":
			case "Snapdragon":
			case "Huasca":
			case "Cadantine":
			case "Lantadyme":
			case "Dwarf weed":
			case "Torstol": minutes = 80; break;

			case "Barley":
			case "Hammerstone": minutes = 40; break;
			case "Asgarnian":
			case "Jute": minutes = 50; break;
			case "Yanillian": minutes = 60; break;
			case "Flax": minutes = 60; break;
			case "Krandorian": minutes = 70; break;
			case "Wildblood":
			case "Hemp": minutes = 80; break;
			case "Cotton": minutes = 100; break;

			case "Redberry": minutes = 100; break;
			case "Cadavaberry": minutes = 120; break;
			case "Dwellberry": minutes = 160; break;
			case "Jangerberry": minutes = 240; break;
			case "Whiteberry": minutes = 320; break;
			case "Poison ivy": minutes = 400; break;

			case "Oak sapling": minutes = 200; break;
			case "Willow sapling": minutes = 240; break;
			case "Maple sapling": minutes = 320; break;
			case "Yew sapling": minutes = 400; break;
			case "Magic sapling": minutes = 480; break;

			case "Apple sapling":
			case "Banana sapling":
			case "Orange sapling":
			case "Curry sapling":
			case "Pineapple sapling":
			case "Papaya sapling":
			case "Palm sapling":
			case "Dragonfruit sapling": minutes = 960; break;

			case "Teak sapling": minutes = 5120; break;
			case "Mahogany sapling":
			case "Camphor sapling":
			case "Ironwood sapling": minutes = 5760; break;
			case "Rosewood sapling": minutes = 6400; break;

			case "Cactus": minutes = 560; break;
			case "Potato cactus": minutes = 70; break;
			case "Mushroom": minutes = 240; break;
			case "Belladonna": minutes = 320; break;
			case "Calquat sapling": minutes = 1280; break;
			case "Spirit sapling": minutes = 3840; break;
			case "Seaweed": minutes = 40; break;
			case "Grape": minutes = 35; break;
			case "Celastrus sapling": minutes = 800; break;
			case "Redwood sapling": minutes = 6400; break;
			case "Hespori": minutes = 1920; break;
			case "Attas":
			case "Iasor":
			case "Kronos": minutes = 5120; break;
			case "Crystal sapling": minutes = 480; break;
			case "Elkhorn fragment":
			case "Pillar fragment":
			case "Umbral fragment": minutes = 160; break;
			default: throw new IllegalArgumentException("Unknown crop " + crop.getName());
		}
		return Duration.ofMinutes(minutes);
	}

	static Duration maximumRemainingAtStage(Crop crop, int currentStage, int totalStages)
	{
		if (currentStage < 1 || totalStages < 2 || currentStage > totalStages)
		{
			throw new IllegalArgumentException("Invalid growth stage " + currentStage + "/" + totalStages);
		}
		return stageDuration(crop, totalStages).multipliedBy(totalStages - currentStage);
	}

	static Duration stageDuration(Crop crop, int totalStages)
	{
		if (totalStages < 2)
		{
			throw new IllegalArgumentException("Invalid total growth stages " + totalStages);
		}
		long totalSeconds = forCrop(crop).getSeconds();
		return Duration.ofSeconds((totalSeconds + totalStages - 2L) / (totalStages - 1L));
	}
}
