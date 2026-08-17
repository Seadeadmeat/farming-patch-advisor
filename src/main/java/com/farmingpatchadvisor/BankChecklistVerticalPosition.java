package com.farmingpatchadvisor;

public enum BankChecklistVerticalPosition
{
	TOP("Top"),
	MIDDLE("Middle"),
	BOTTOM("Bottom");

	private final String displayName;

	BankChecklistVerticalPosition(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
