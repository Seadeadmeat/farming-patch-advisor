package com.farmingpatchadvisor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompostBinCodecTest
{
	@Test
	public void decodesStandardCompostBinStates()
	{
		assertState(0, false, CompostProduct.EMPTY, CompostBinPhase.EMPTY, 0, -1);
		assertState(15, false, CompostProduct.COMPOST, CompostBinPhase.FILLING, 15, -1);
		assertState(31, false, CompostProduct.COMPOST, CompostBinPhase.PROCESSING, 15, 0);
		assertState(94, false, CompostProduct.COMPOST, CompostBinPhase.READY, 15, 2);
		assertState(47, false, CompostProduct.SUPERCOMPOST, CompostBinPhase.FILLING, 15, -1);
		assertState(95, false, CompostProduct.SUPERCOMPOST, CompostBinPhase.PROCESSING, 15, 0);
		assertState(126, false, CompostProduct.SUPERCOMPOST, CompostBinPhase.READY, 15, 2);
		assertState(176, false, CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 15, -1);
		assertState(190, false, CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 1, -1);
	}

	@Test
	public void decodesFarmingGuildBigCompostBinStates()
	{
		assertState(77, true, CompostProduct.COMPOST, CompostBinPhase.FILLING, 30, -1);
		assertState(127, true, CompostProduct.COMPOST, CompostBinPhase.PROCESSING, 30, 0);
		assertState(93, true, CompostProduct.COMPOST, CompostBinPhase.READY, 30, 2);
		assertState(175, true, CompostProduct.SUPERCOMPOST, CompostBinPhase.FILLING, 30, -1);
		assertState(97, true, CompostProduct.SUPERCOMPOST, CompostBinPhase.PROCESSING, 30, 0);
		assertState(99, true, CompostProduct.SUPERCOMPOST, CompostBinPhase.READY, 30, 2);
		assertState(176, true, CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 30, -1);
		assertState(205, true, CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 1, -1);
	}

	@Test
	public void unknownValuesDoNotCreateInventedState()
	{
		assertState(500, false, CompostProduct.UNKNOWN, CompostBinPhase.UNKNOWN, 0, -1);
		assertState(500, true, CompostProduct.UNKNOWN, CompostBinPhase.UNKNOWN, 0, -1);
	}

	private static void assertState(int value, boolean big, CompostProduct product,
		CompostBinPhase phase, int amount, int stage)
	{
		CompostBinCodec.Decoded decoded = CompostBinCodec.decode(value, big);
		assertEquals(product, decoded.getProduct());
		assertEquals(phase, decoded.getPhase());
		assertEquals(amount, decoded.getAmount());
		assertEquals(stage, decoded.getStage());
	}
}
