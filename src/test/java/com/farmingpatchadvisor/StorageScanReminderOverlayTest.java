package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StorageScanReminderOverlayTest
{
	@Test
	public void identifiesTheStorageSourceThatStillNeedsScanning()
	{
		assertEquals("Open your bank and Seed Vault",
			StorageScanReminderOverlay.scanInstruction(false, false));
		assertEquals("Open your Seed Vault",
			StorageScanReminderOverlay.scanInstruction(true, false));
		assertEquals("Open your bank",
			StorageScanReminderOverlay.scanInstruction(false, true));
	}
}
