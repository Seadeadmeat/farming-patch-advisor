package com.farmingpatchadvisor;

import java.time.Duration;
import java.time.Instant;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;
import net.runelite.api.Point;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatchTimerTest
{
	@Test
	public void groupsAdjacentPatchObjectsUsingTheirFullSceneFootprints()
	{
		assertTrue(FarmingPatchOverlay.sceneAreasTouch(
			new Point(10, 10), new Point(13, 13), new Point(14, 11), new Point(16, 13)));
		assertTrue(FarmingPatchOverlay.sceneAreasTouch(
			new Point(10, 10), new Point(13, 13), new Point(14, 14), new Point(16, 16)));
		assertFalse(FarmingPatchOverlay.sceneAreasTouch(
			new Point(10, 10), new Point(13, 13), new Point(15, 10), new Point(17, 13)));
	}

	@Test
	public void appliesAPlantedContractOnlyToItsMatchingAllotment()
	{
		WorldPoint plantedPatch = new WorldPoint(1240, 3720, 0);
		assertTrue(FarmingPatchOverlay.belongsToPlantedContractPatch(
			new WorldPoint(1241, 3721, 0), plantedPatch));
		assertFalse(FarmingPatchOverlay.belongsToPlantedContractPatch(
			new WorldPoint(1245, 3720, 0), plantedPatch));
		assertFalse(FarmingPatchOverlay.belongsToPlantedContractPatch(
			new WorldPoint(1240, 3720, 1), plantedPatch));
	}

	@Test
	public void advancesInspectedStageFromElapsedTime()
	{
		Crop potato = CropCatalog.recommend(PatchType.ALLOTMENT, 1);
		Instant inspected = Instant.parse("2026-08-12T00:00:00Z");
		PatchTimer timer = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.ALLOTMENT, potato,
			inspected, inspected.plus(Duration.ofMinutes(30)), false, 2, 5);
		assertEquals(2, timer.getEstimatedStage(inspected.plus(Duration.ofMinutes(9))));
		assertEquals(3, timer.getEstimatedStage(inspected.plus(Duration.ofMinutes(10))));
		assertEquals(5, timer.getEstimatedStage(inspected.plus(Duration.ofHours(1))));
	}

	@Test
	public void deadStateIsPersistentTimerData()
	{
		Crop potato = CropCatalog.findByName(PatchType.ALLOTMENT, "Potato");
		Instant now = Instant.parse("2026-08-19T00:00:00Z");
		PatchTimer timer = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.ALLOTMENT,
			potato, now, now.plusSeconds(600), true, -1, -1, true);

		assertTrue(timer.isDead());
		assertFalse(timer.getReadyAt().isBefore(now));
	}

	@Test
	public void recognizesDeadInspectionMessages()
	{
		assertTrue(PatchTimerManager.isDeadStateMessage("The maple tree has died."));
		assertTrue(PatchTimerManager.isDeadStateMessage("This plant is dead."));
		assertTrue(PatchTimerManager.isDeadStateMessage("You inspect the dead plant."));
		assertFalse(PatchTimerManager.isDeadStateMessage("The plant is growing well."));
	}

	@Test
	public void diseasedStateIsPersistentTimerData()
	{
		Crop ranarr = CropCatalog.findByName(PatchType.HERB, "Ranarr");
		Instant now = Instant.parse("2026-08-19T00:00:00Z");
		PatchTimer timer = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.HERB,
			ranarr, now, now.plusSeconds(600), true, -1, -1, false, true);

		assertFalse(timer.isDead());
		assertTrue(timer.isDiseased());
		assertEquals("Plant cure / Cure Plant", PatchRemedy.forTimer(timer));
	}

	@Test
	public void recognizesDiseasedInspectionMessages()
	{
		assertTrue(PatchTimerManager.isDiseasedStateMessage("The herb has become diseased."));
		assertTrue(PatchTimerManager.isDiseasedStateMessage("This plant is diseased."));
		assertTrue(PatchTimerManager.isDiseasedStateMessage("The diseased leaves need treating."));
		assertFalse(PatchTimerManager.isDiseasedStateMessage("The plant is growing well."));
	}

	@Test
	public void recognizesLiveObjectHealthActionsWithoutInspection()
	{
		assertTrue(PatchTimerManager.isDeadObjectState("Herbs",
			new String[]{"Clear", "Inspect", "Guide"}));
		assertTrue(PatchTimerManager.isDiseasedObjectState("Herbs",
			new String[]{"Cure", "Inspect", "Guide"}));
		assertFalse(PatchTimerManager.isDeadObjectState("Tree stump",
			new String[]{"Clear", "Inspect", "Guide"}));
		assertFalse(PatchTimerManager.isDeadObjectState("Herbs",
			new String[]{"Pick", "Inspect", "Guide"}));
	}

	@Test
	public void providesPatchSpecificRemedies()
	{
		Instant now = Instant.parse("2026-08-19T00:00:00Z");
		Crop maple = CropCatalog.findByName(PatchType.TREE, "Maple");
		PatchTimer diseasedTree = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.TREE,
			maple, now, now, false, -1, -1, false, true);
		assertEquals("Secateurs / Plant cure / Cure Plant", PatchRemedy.forTimer(diseasedTree));

		Crop redwood = CropCatalog.findByName(PatchType.REDWOOD, "Redwood");
		PatchTimer deadRedwood = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.REDWOOD,
			redwood, now, now, false, -1, -1, true, false);
		assertEquals("Pay Alexandra to clear", PatchRemedy.forTimer(deadRedwood));
	}

	@Test
	public void tracksOutstandingPatchCare()
	{
		Instant now = Instant.parse("2026-08-19T00:00:00Z");
		Crop potato = CropCatalog.findByName(PatchType.ALLOTMENT, "Potato");
		PatchTimer untreated = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.ALLOTMENT,
			potato, now, now.plusSeconds(600));
		assertTrue(untreated.needsCompost());
		assertTrue(untreated.needsWater());

		PatchTimer treated = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.ALLOTMENT,
			potato, now, now.plusSeconds(600), true, -1, -1, false, false, true, true);
		assertFalse(treated.needsCompost());
		assertFalse(treated.needsWater());
	}

	@Test
	public void parsesInspectionStagesAndCompletionMessages()
	{
		assertEquals(2, PatchTimerManager.parseGrowthStage("Growth stage 2 of 5")[0]);
		assertEquals(5, PatchTimerManager.parseGrowthStage("Growth stage 2 of 5")[1]);
		assertEquals(3, PatchTimerManager.parseGrowthStage("It is at 3/7.")[0]);
		assertTrue(PatchTimerManager.isCompletedStateMessage("This tree is fully grown."));
		assertTrue(PatchTimerManager.isCompletedStateMessage("It is ready to harvest."));
	}

	@Test
	public void recognizesInspectedPatchCare()
	{
		assertTrue(PatchTimerManager.isCompostedMessage(
			"You treat the herb patch with ultracompost."));
		assertTrue(PatchTimerManager.isCompostedMessage(
			"This is a ranarr plant. The soil has been treated with ultracompost."));
		assertTrue(PatchTimerManager.isWateredMessage("The allotment has been watered."));
	}

	@Test
	public void onlyCollapsesPatchTypesWithOnePhysicalPatchPerLocation()
	{
		assertTrue(PatchTimerManager.hasSinglePatchPerLocation(PatchType.TREE));
		assertTrue(PatchTimerManager.hasSinglePatchPerLocation(PatchType.HERB));
		assertFalse(PatchTimerManager.hasSinglePatchPerLocation(PatchType.ALLOTMENT));
		assertFalse(PatchTimerManager.hasSinglePatchPerLocation(PatchType.SEAWEED));
		assertFalse(PatchTimerManager.hasSinglePatchPerLocation(PatchType.HARDWOOD_TREE));
	}

	@Test
	public void contractPatchRequiresClearingWhenAnotherCropIsGrowing()
	{
		Instant now = Instant.parse("2026-08-20T00:00:00Z");
		Crop maple = CropCatalog.findByName(PatchType.TREE, "Maple sapling");
		Crop yew = CropCatalog.findByName(PatchType.TREE, "Yew sapling");
		PatchTimer timer = new PatchTimer(new WorldPoint(0, 0, 0), PatchType.TREE,
			maple, now, now.plus(Duration.ofHours(5)));

		assertTrue(FarmingPatchOverlay.requiresClearingForContract(timer.getCrop(), yew));
		assertFalse(FarmingPatchOverlay.requiresClearingForContract(timer.getCrop(), maple));
		assertFalse(FarmingPatchOverlay.requiresClearingForContract((Crop) null, yew));
		assertTrue(FarmingPatchOverlay.usesContractGroundOutline(true, maple));
		assertFalse(FarmingPatchOverlay.usesContractGroundOutline(false, maple));
		assertFalse(FarmingPatchOverlay.usesContractGroundOutline(true, null));
	}
}
