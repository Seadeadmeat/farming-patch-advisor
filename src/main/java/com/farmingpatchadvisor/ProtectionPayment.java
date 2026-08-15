package com.farmingpatchadvisor;

final class ProtectionPayment
{
	private final String name;
	private final int itemId;
	private final int quantity;

	ProtectionPayment(String name, int itemId, int quantity)
	{
		this.name = name;
		this.itemId = itemId;
		this.quantity = quantity;
	}

	String getName()
	{
		return name;
	}

	int getItemId()
	{
		return itemId;
	}

	int getQuantity()
	{
		return quantity;
	}
}
