package com.farmingpatchadvisor;

final class CompostBinCodec
{
	private CompostBinCodec()
	{
	}

	static Decoded decode(int value, boolean big)
	{
		return big ? decodeBig(value) : decodeStandard(value);
	}

	private static Decoded decodeStandard(int value)
	{
		if (value == 0) return decoded(CompostProduct.EMPTY, CompostBinPhase.EMPTY, 0, -1);
		if (between(value, 1, 15)) return decoded(CompostProduct.COMPOST, CompostBinPhase.FILLING, value, -1);
		if (between(value, 16, 30)) return decoded(CompostProduct.COMPOST, CompostBinPhase.COLLECTING, 31 - value, -1);
		if (between(value, 31, 32)) return decoded(CompostProduct.COMPOST, CompostBinPhase.PROCESSING, 15, value - 31);
		if (between(value, 33, 47)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.FILLING, value - 32, -1);
		if (between(value, 48, 62)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.COLLECTING, 63 - value, -1);
		if (value == 94) return decoded(CompostProduct.COMPOST, CompostBinPhase.READY, 15, 2);
		if (between(value, 95, 96)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.PROCESSING, 15, value - 95);
		if (value == 126) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.READY, 15, 2);
		if (between(value, 129, 143)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.FILLING, value - 128, -1);
		if (between(value, 144, 158)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.COLLECTING, 159 - value, -1);
		if (between(value, 159, 160)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.PROCESSING, 15, value - 159);
		if (between(value, 176, 190)) return decoded(CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 191 - value, -1);
		return decoded(CompostProduct.UNKNOWN, CompostBinPhase.UNKNOWN, 0, -1);
	}

	private static Decoded decodeBig(int value)
	{
		if (value == 0) return decoded(CompostProduct.EMPTY, CompostBinPhase.EMPTY, 0, -1);
		if (between(value, 1, 15)) return decoded(CompostProduct.COMPOST, CompostBinPhase.FILLING, value, -1);
		if (between(value, 16, 30)) return decoded(CompostProduct.COMPOST, CompostBinPhase.COLLECTING, 46 - value, -1);
		if (between(value, 33, 47)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.FILLING, value - 32, -1);
		if (between(value, 48, 62)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.COLLECTING, 78 - value, -1);
		if (between(value, 63, 77)) return decoded(CompostProduct.COMPOST, CompostBinPhase.FILLING, value - 47, -1);
		if (between(value, 78, 92)) return decoded(CompostProduct.COMPOST, CompostBinPhase.COLLECTING, 93 - value, -1);
		if (value == 93) return decoded(CompostProduct.COMPOST, CompostBinPhase.READY, 30, 2);
		if (between(value, 97, 98)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.PROCESSING, 30, value - 97);
		if (value == 99) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.READY, 30, 2);
		if (between(value, 100, 114)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.COLLECTING, 115 - value, -1);
		if (between(value, 127, 128)) return decoded(CompostProduct.COMPOST, CompostBinPhase.PROCESSING, 30, value - 127);
		if (between(value, 129, 143)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.FILLING, value - 128, -1);
		if (between(value, 144, 158)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.COLLECTING, 174 - value, -1);
		if (between(value, 159, 160)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.PROCESSING, 30, value - 159);
		if (between(value, 161, 175)) return decoded(CompostProduct.SUPERCOMPOST, CompostBinPhase.FILLING, value - 145, -1);
		if (between(value, 176, 205)) return decoded(CompostProduct.ULTRACOMPOST, CompostBinPhase.COLLECTING, 206 - value, -1);
		if (between(value, 207, 221)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.COLLECTING, 222 - value, -1);
		if (value == 222) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.READY, 30, 2);
		if (between(value, 223, 237)) return decoded(CompostProduct.ROTTEN_TOMATO, CompostBinPhase.FILLING, value - 207, -1);
		return decoded(CompostProduct.UNKNOWN, CompostBinPhase.UNKNOWN, 0, -1);
	}

	private static boolean between(int value, int minimum, int maximum)
	{
		return value >= minimum && value <= maximum;
	}

	private static Decoded decoded(CompostProduct product, CompostBinPhase phase, int amount, int stage)
	{
		return new Decoded(product, phase, amount, stage);
	}

	static final class Decoded
	{
		private final CompostProduct product;
		private final CompostBinPhase phase;
		private final int amount;
		private final int stage;

		private Decoded(CompostProduct product, CompostBinPhase phase, int amount, int stage)
		{
			this.product = product;
			this.phase = phase;
			this.amount = amount;
			this.stage = stage;
		}

		CompostProduct getProduct() { return product; }
		CompostBinPhase getPhase() { return phase; }
		int getAmount() { return amount; }
		int getStage() { return stage; }
	}
}
