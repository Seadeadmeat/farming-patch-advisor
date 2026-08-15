package com.farmingpatchadvisor;

import java.util.List;
import net.runelite.api.gameval.ItemID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProtectionPaymentCatalogTest
{
	@Test
	public void mapsRecommendedCropsToTheirProtectionPayments()
	{
		assertPayment(PatchType.ALLOTMENT, 99, ItemID.JANGERBERRIES, 5);
		assertPayment(PatchType.TREE, 99, ItemID.COCONUT, 25);
		assertPayment(PatchType.FRUIT_TREE, 99, ItemID.COCONUT, 15);
		assertPayment(PatchType.HARDWOOD_TREE, 99, ItemID.DRAGONFRUIT, 8);
		assertPayment(PatchType.CORAL, 99, ItemID.CORAL_PILLAR, 5);
	}

	@Test
	public void omitsCropsThatCannotBeProtectedByPayment()
	{
		assertTrue(ProtectionPaymentCatalog.forCrop(CropCatalog.recommend(PatchType.HERB, 99)).isEmpty());
		assertTrue(ProtectionPaymentCatalog.forCrop(CropCatalog.recommend(PatchType.FLOWER, 99)).isEmpty());
		assertTrue(ProtectionPaymentCatalog.forCrop(CropCatalog.recommend(PatchType.CRYSTAL_TREE, 99)).isEmpty());
	}

	private static void assertPayment(PatchType patchType, int level, int itemId, int quantity)
	{
		List<ProtectionPayment> payments = ProtectionPaymentCatalog.forCrop(CropCatalog.recommend(patchType, level));
		assertTrue(payments.stream().anyMatch(payment -> payment.getItemId() == itemId
			&& payment.getQuantity() == quantity));
		assertEquals(1, payments.size());
	}
}
