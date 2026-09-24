package com.farmingpatchadvisor;

import java.time.Instant;

final class CompostBinState
{
	private final CompostBinLocation location;
	private final CompostProduct product;
	private final CompostBinPhase phase;
	private final int amount;
	private final int stage;
	private final Instant observedAt;
	private final Instant readyAt;

	CompostBinState(CompostBinLocation location, CompostProduct product, CompostBinPhase phase,
		int amount, int stage, Instant observedAt, Instant readyAt)
	{
		this.location = location;
		this.product = product;
		this.phase = phase;
		this.amount = amount;
		this.stage = stage;
		this.observedAt = observedAt;
		this.readyAt = readyAt;
	}

	CompostBinLocation getLocation() { return location; }
	CompostProduct getProduct() { return product; }
	CompostBinPhase getPhase() { return phase; }
	int getAmount() { return amount; }
	int getStage() { return stage; }
	Instant getObservedAt() { return observedAt; }
	Instant getReadyAt() { return readyAt; }

	boolean isReady(Instant now)
	{
		return phase == CompostBinPhase.READY || phase == CompostBinPhase.COLLECTING
			|| phase == CompostBinPhase.PROCESSING && readyAt != null && !now.isBefore(readyAt);
	}
}
