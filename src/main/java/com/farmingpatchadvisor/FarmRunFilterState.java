package com.farmingpatchadvisor;

import javax.inject.Singleton;

@Singleton
final class FarmRunFilterState
{
	private FarmRunFilter selected = FarmRunFilter.ALL;

	synchronized FarmRunFilter getSelected()
	{
		return selected;
	}

	synchronized void setSelected(FarmRunFilter selected)
	{
		this.selected = selected == null ? FarmRunFilter.ALL : selected;
	}

	synchronized boolean includes(PatchType patchType)
	{
		return selected.includes(FarmRunType.forPatchType(patchType));
	}
}
