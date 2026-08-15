package com.farmingpatchadvisor;

import java.time.Instant;
import net.runelite.api.coords.WorldPoint;

final class PatchTimer
{
	private final WorldPoint patchLocation;
	private final PatchType patchType;
	private final Crop crop;
	private final Instant plantedAt;
	private final Instant readyAt;
	private final boolean plantedTimer;
	private final int observedStage;
	private final int totalStages;

	PatchTimer(WorldPoint patchLocation, PatchType patchType, Crop crop, Instant plantedAt, Instant readyAt)
	{
		this(patchLocation, patchType, crop, plantedAt, readyAt, true, -1, -1);
	}

	PatchTimer(WorldPoint patchLocation, PatchType patchType, Crop crop, Instant plantedAt, Instant readyAt,
		boolean plantedTimer, int observedStage, int totalStages)
	{
		this.patchLocation = patchLocation;
		this.patchType = patchType;
		this.crop = crop;
		this.plantedAt = plantedAt;
		this.readyAt = readyAt;
		this.plantedTimer = plantedTimer;
		this.observedStage = observedStage;
		this.totalStages = totalStages;
	}

	String key()
	{
		return patchLocation.getX() + ":" + patchLocation.getY() + ":" + patchLocation.getPlane() + ":" + patchType.name();
	}

	WorldPoint getPatchLocation()
	{
		return patchLocation;
	}

	PatchType getPatchType()
	{
		return patchType;
	}

	Crop getCrop()
	{
		return crop;
	}

	Instant getPlantedAt()
	{
		return plantedAt;
	}

	Instant getReadyAt()
	{
		return readyAt;
	}

	boolean isPlantedTimer()
	{
		return plantedTimer;
	}

	int getObservedStage()
	{
		return observedStage;
	}

	int getTotalStages()
	{
		return totalStages;
	}

	int getEstimatedStage(Instant now)
	{
		if (plantedTimer || observedStage < 1 || totalStages < 2)
		{
			return observedStage;
		}
		long stageSeconds = CropGrowthTimes.stageDuration(crop, totalStages).getSeconds();
		long elapsedSeconds = Math.max(0, java.time.Duration.between(plantedAt, now).getSeconds());
		long stagesElapsed = stageSeconds == 0 ? 0 : elapsedSeconds / stageSeconds;
		return (int) Math.min(totalStages, observedStage + stagesElapsed);
	}
}
