package com.farmingpatchadvisor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import net.runelite.client.ui.ColorScheme;

/** A draft editor; only Save writes character preferences. */
final class FarmRouteEditor
{
	private FarmRouteEditor() { }

	static void open(Component parent, FarmRouteManager routes, FarmRunFilter run)
	{
		String profile = routes.profile();
		if (profile == null)
		{
			JOptionPane.showMessageDialog(parent, "Log in to a character to edit its route.");
			return;
		}
		List<FarmRunFilter> availableRuns = availableRuns(routes);
		if (availableRuns.isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "Enable patches and locations in settings first.");
			return;
		}
		if (!availableRuns.contains(run))
		{
			run = availableRuns.contains(FarmRunFilter.ALL) ? FarmRunFilter.ALL : availableRuns.get(0);
		}
		List<FarmRunPatch> active = routes.activePatches(run);
		if (active.isEmpty() && !routes.isCustom(run))
		{
			JOptionPane.showMessageDialog(parent, "Enable patches and locations in settings first.");
			return;
		}
		DefaultListModel<FarmRunPatch> model = new DefaultListModel<>();
		Map<FarmRunFilter, RouteDraft> drafts = new EnumMap<>(FarmRunFilter.class);
		RouteEditorState state = new RouteEditorState(run);
		RouteDraft initialDraft = createDraft(routes, run);
		drafts.put(run, initialDraft);
		state.choices = initialDraft.choices;
		state.groups = initialDraft.groups;
		initialDraft.patches.forEach(model::addElement);
		JList<FarmRunPatch> list = new JList<>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		list.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override public Component getListCellRendererComponent(JList<?> l, Object value, int index,
				boolean selected, boolean focus)
			{
				FarmRunPatch p = (FarmRunPatch) value;
				int width = Math.max(170, l.getWidth() - 30);
				boolean grouped = state.groups.containsKey(FarmRouteManager.key(p));
				String name = (grouped ? p.getLocation() : p.getDisplayName())
					.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
				return super.getListCellRendererComponent(l,
					"<html><body style='width:" + width + "px'>" + (index + 1) + ". " + name
						+ (grouped ? "" : " — " + p.getPatchType().getDisplayName()) + "</body></html>",
					index, selected, focus);
			}
		});
		JComboBox<String> order = new JComboBox<>(new String[]{"Default", "Custom"});
		order.setSelectedItem(initialDraft.custom ? "Custom" : "Default");
		JComboBox<FarmRunFilter> runSelector = new JComboBox<>(availableRuns.toArray(new FarmRunFilter[0]));
		runSelector.setSelectedItem(state.run);
		JComboBox<RouteTeleport> teleport = new JComboBox<>();
		boolean[] loading = {false};
		list.addListSelectionListener(e ->
		{
			FarmRunPatch p = list.getSelectedValue();
			if (p != null)
			{
				loading[0] = true;
				teleport.setModel(new DefaultComboBoxModel<>(RouteTeleport.viableValues(p)));
				teleport.setSelectedItem(state.choices.getOrDefault(FarmRouteManager.key(p), RouteTeleport.NONE));
				loading[0] = false;
			}
		});
		teleport.addActionListener(e ->
		{
			FarmRunPatch p = list.getSelectedValue();
			if (!loading[0] && p != null)
			{
				state.choices.put(FarmRouteManager.key(p), (RouteTeleport) teleport.getSelectedItem());
			}
		});
		order.addActionListener(e ->
		{
			if (loading[0])
			{
				return;
			}
			if ("Default".equals(order.getSelectedItem()))
			{
				RouteDraft defaults = createDraft(routes, state.run, routes.defaultPatches(state.run), false);
				state.choices = defaults.choices;
				state.groups = defaults.groups;
				model.clear();
				defaults.patches.forEach(model::addElement);
				list.setSelectedIndex(0);
			}
		});
		JButton up = new JButton("Move up");
		JButton down = new JButton("Move down");
		JButton newRoute = new JButton("New custom route");
		JButton resetRoute = new JButton("Reset selected run");
		JButton addPatches = new JButton("Add patches...");
		JButton removePatch = new JButton("Remove selected");
		up.addActionListener(e -> move(model, list, list.getSelectedIndex() - 1, order));
		down.addActionListener(e -> move(model, list, list.getSelectedIndex() + 1, order));
		newRoute.addActionListener(e ->
		{
			model.clear();
			teleport.setModel(new DefaultComboBoxModel<>());
			order.setSelectedItem("Custom");
		});
		resetRoute.addActionListener(e ->
		{
			List<FarmRunPatch> defaults = routes.defaultPatches(state.run);
			Map<String, RouteTeleport> resetChoices = new LinkedHashMap<>();
			for (FarmRunPatch patch : defaults)
			{
				resetChoices.put(FarmRouteManager.key(patch), RouteTeleport.NONE);
			}
			RouteDraft reset = groupedDraft(defaults, resetChoices, false);
			state.choices = reset.choices;
			state.groups = reset.groups;
			loading[0] = true;
			model.clear();
			reset.patches.forEach(model::addElement);
			order.setSelectedItem("Default");
			loading[0] = false;
			if (!model.isEmpty()) list.setSelectedIndex(0);
		});
		addPatches.addActionListener(e -> addPatches(parent, routes, state.run, model, list,
			state.choices, state.groups, order));
		removePatch.addActionListener(e ->
		{
			int selected = list.getSelectedIndex();
			if (selected < 0) { return; }
			model.remove(selected);
			order.setSelectedItem("Custom");
			if (!model.isEmpty()) { list.setSelectedIndex(Math.min(selected, model.size() - 1)); }
		});
		runSelector.addActionListener(e ->
		{
			if (loading[0] || runSelector.getSelectedItem() == state.run)
			{
				return;
			}
			storeDraft(drafts, state, model, order);
			FarmRunFilter selectedRun = (FarmRunFilter) runSelector.getSelectedItem();
			RouteDraft draft = drafts.computeIfAbsent(selectedRun, value -> createDraft(routes, value));
			state.run = selectedRun;
			state.choices = draft.choices;
			state.groups = draft.groups;
			loading[0] = true;
			model.clear();
			draft.patches.forEach(model::addElement);
			order.setSelectedItem(draft.custom ? "Custom" : "Default");
			loading[0] = false;
			if (!model.isEmpty())
			{
				list.setSelectedIndex(0);
			}
			else
			{
				teleport.setModel(new DefaultComboBoxModel<>());
			}
		});
		list.setDragEnabled(true);
		list.setDropMode(DropMode.INSERT);
		list.setTransferHandler(new TransferHandler()
		{
			@Override public int getSourceActions(JComponent c) { return MOVE; }
			@Override protected Transferable createTransferable(JComponent c)
			{
				FarmRunPatch p = list.getSelectedValue();
				return p == null ? null : new StringSelection(FarmRouteManager.key(p));
			}
			@Override public boolean canImport(TransferSupport support)
			{
				return support.isDrop() && support.isDataFlavorSupported(DataFlavor.stringFlavor);
			}
			@Override public boolean importData(TransferSupport support)
			{
				if (!canImport(support)) { return false; }
				try
				{
					String key = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
					for (int i = 0; i < model.size(); i++)
					{
						if (FarmRouteManager.key(model.get(i)).equals(key))
						{
							int target = ((JList.DropLocation) support.getDropLocation()).getIndex();
							list.setSelectedIndex(i);
							move(model, list, target > i ? target - 1 : target, order);
							return true;
						}
					}
				}
				catch (Exception ex) { return false; }
				return false;
			}
		});
		JPanel header = new JPanel(new GridLayout(0, 1, 0, 6));
		header.add(new JLabel("Farm run:"));
		header.add(runSelector);
		header.add(new JLabel("Route order:"));
		header.add(order);
		header.add(new JLabel("Drag stops or use Move up / Move down."));
		JPanel controls = new JPanel(new GridLayout(0, 1, 0, 6));
		JPanel arrows = new JPanel(new GridLayout(1, 2, 6, 0));
		arrows.add(up); arrows.add(down); controls.add(arrows);
		controls.add(newRoute);
		controls.add(resetRoute);
		JPanel membership = new JPanel(new GridLayout(1, 2, 6, 0));
		membership.add(addPatches); membership.add(removePatch); controls.add(membership);
		controls.add(new JLabel("Teleport for selected patch:"));
		controls.add(teleport);
		controls.add(new JLabel("Check remaining charges and daily teleport limits."));
		JPanel editor = new JPanel(new BorderLayout(0, 8));
		editor.add(header, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(list);
		FarmingPatchPanel.styleNarrowScrollBar(scroll);
		Rectangle screen = parent.getGraphicsConfiguration().getBounds();
		int width = Math.max(260, Math.min(420, screen.width - 120));
		int height = Math.max(170, Math.min(320, screen.height - 320));
		scroll.setPreferredSize(new Dimension(width, height));
		editor.add(scroll, BorderLayout.CENTER);
		editor.add(controls, BorderLayout.SOUTH);
		list.setSelectedIndex(0);
		int result = JOptionPane.showOptionDialog(parent, editor, "Farm Run — Route & Teleports",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
			new String[]{"Save route", "Cancel"}, "Save route");
		if (result == 0)
		{
			storeDraft(drafts, state, model, order);
			boolean saved = true;
			for (Map.Entry<FarmRunFilter, RouteDraft> entry : drafts.entrySet())
			{
				RouteDraft draft = entry.getValue();
				saved &= routes.save(profile, entry.getKey(), draft.expandedPatches(),
					draft.expandedChoices(), draft.custom);
			}
			if (!saved)
			{
				JOptionPane.showMessageDialog(parent, "Character changed. Reopen the editor to save this character's route.");
			}
		}
	}

	private static List<FarmRunFilter> availableRuns(FarmRouteManager routes)
	{
		Set<FarmRunType> runTypes = EnumSet.noneOf(FarmRunType.class);
		for (FarmRunPatch patch : routes.availablePatches())
		{
			runTypes.add(patch.getFarmRunType());
		}
		return availableRuns(runTypes);
	}

	static List<FarmRunFilter> availableRuns(Set<FarmRunType> runTypes)
	{
		List<FarmRunFilter> filters = new ArrayList<>();
		for (FarmRunFilter filter : FarmRunFilter.values())
		{
			if (!filter.isCompostOnly() && filter.isAvailable(runTypes))
			{
				filters.add(filter);
			}
		}
		return filters;
	}

	private static RouteDraft createDraft(FarmRouteManager routes, FarmRunFilter run)
	{
		return createDraft(routes, run, routes.activePatches(run), routes.isCustom(run));
	}

	private static RouteDraft createDraft(FarmRouteManager routes, FarmRunFilter run,
		List<FarmRunPatch> patches, boolean custom)
	{
		Map<String, RouteTeleport> choices = new LinkedHashMap<>();
		for (FarmRunPatch patch : patches)
		{
			choices.put(FarmRouteManager.key(patch), routes.teleport(run, patch));
		}
		return groupedDraft(patches, choices, custom);
	}

	static RouteDraft groupedDraft(List<FarmRunPatch> patches,
		Map<String, RouteTeleport> originalChoices, boolean custom)
	{
		Map<String, Boolean> allotments = new LinkedHashMap<>();
		Map<String, Boolean> flowers = new LinkedHashMap<>();
		for (FarmRunPatch patch : patches)
		{
			if (patch.getPatchType() == PatchType.ALLOTMENT) allotments.put(patch.getLocation(), true);
			if (patch.getPatchType() == PatchType.FLOWER) flowers.put(patch.getLocation(), true);
		}
		List<FarmRunPatch> displayed = new ArrayList<>();
		Map<String, List<FarmRunPatch>> groups = new LinkedHashMap<>();
		Map<String, RouteTeleport> displayedChoices = new LinkedHashMap<>();
		for (FarmRunPatch patch : patches)
		{
			boolean grouped = (patch.getPatchType() == PatchType.ALLOTMENT || patch.getPatchType() == PatchType.FLOWER)
				&& allotments.containsKey(patch.getLocation()) && flowers.containsKey(patch.getLocation());
			if (!grouped)
			{
				displayed.add(patch);
				displayedChoices.put(FarmRouteManager.key(patch),
					originalChoices.getOrDefault(FarmRouteManager.key(patch), RouteTeleport.NONE));
				continue;
			}
			FarmRunPatch stop = new FarmRunPatch(patch.getLocation(), "", PatchType.ALLOTMENT);
			String stopKey = FarmRouteManager.key(stop);
			if (!groups.containsKey(stopKey))
			{
				displayed.add(stop);
				groups.put(stopKey, new ArrayList<>());
				displayedChoices.put(stopKey,
					originalChoices.getOrDefault(FarmRouteManager.key(patch), RouteTeleport.NONE));
			}
			groups.get(stopKey).add(patch);
		}
		return new RouteDraft(displayed, displayedChoices, groups, custom);
	}

	private static void storeDraft(Map<FarmRunFilter, RouteDraft> drafts, RouteEditorState state,
		DefaultListModel<FarmRunPatch> model, JComboBox<String> order)
	{
		List<FarmRunPatch> patches = new ArrayList<>();
		for (int i = 0; i < model.size(); i++)
		{
			patches.add(model.get(i));
		}
		drafts.put(state.run, new RouteDraft(patches, state.choices, state.groups,
			"Custom".equals(order.getSelectedItem())));
	}

	private static final class RouteEditorState
	{
		private FarmRunFilter run;
		private Map<String, RouteTeleport> choices;
		private Map<String, List<FarmRunPatch>> groups;

		private RouteEditorState(FarmRunFilter run)
		{
			this.run = run;
		}
	}

	static final class RouteDraft
	{
		private final List<FarmRunPatch> patches;
		private final Map<String, RouteTeleport> choices;
		private final Map<String, List<FarmRunPatch>> groups;
		private final boolean custom;

		private RouteDraft(List<FarmRunPatch> patches, Map<String, RouteTeleport> choices,
			Map<String, List<FarmRunPatch>> groups, boolean custom)
		{
			this.patches = new ArrayList<>(patches);
			this.choices = new LinkedHashMap<>(choices);
			this.groups = new LinkedHashMap<>();
			for (Map.Entry<String, List<FarmRunPatch>> group : groups.entrySet())
			{
				this.groups.put(group.getKey(), new ArrayList<>(group.getValue()));
			}
			this.custom = custom;
		}

		List<FarmRunPatch> displayedPatches() { return new ArrayList<>(patches); }

		List<FarmRunPatch> expandedPatches()
		{
			List<FarmRunPatch> expanded = new ArrayList<>();
			for (FarmRunPatch patch : patches)
			{
				List<FarmRunPatch> members = groups.get(FarmRouteManager.key(patch));
				if (members == null) expanded.add(patch); else expanded.addAll(members);
			}
			return expanded;
		}

		Map<String, RouteTeleport> expandedChoices()
		{
			Map<String, RouteTeleport> expanded = new LinkedHashMap<>();
			for (FarmRunPatch patch : patches)
			{
				String key = FarmRouteManager.key(patch);
				RouteTeleport choice = choices.getOrDefault(key, RouteTeleport.NONE);
				List<FarmRunPatch> members = groups.get(key);
				if (members == null)
				{
					expanded.put(key, choice);
				}
				else
				{
					for (FarmRunPatch member : members)
					{
						expanded.put(FarmRouteManager.key(member), choice);
					}
				}
			}
			return expanded;
		}
	}

	private static void addPatches(Component parent, FarmRouteManager routes, FarmRunFilter run,
		DefaultListModel<FarmRunPatch> model, JList<FarmRunPatch> route,
		Map<String, RouteTeleport> choices, Map<String, List<FarmRunPatch>> groups,
		JComboBox<String> order)
	{
		List<FarmRunPatch> available = routes.availablePatches();
		available.removeIf(p -> !run.includes(p.getFarmRunType()));
		Map<String, RouteTeleport> availableChoices = new LinkedHashMap<>();
		for (FarmRunPatch patch : available)
		{
			availableChoices.put(FarmRouteManager.key(patch), routes.teleport(run, patch));
		}
		RouteDraft availableDraft = groupedDraft(available, availableChoices, true);
		available = availableDraft.patches;
		for (int i = 0; i < model.size(); i++)
		{
			String existing = FarmRouteManager.key(model.get(i));
			available.removeIf(p -> FarmRouteManager.key(p).equals(existing));
		}
		if (available.isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "Every enabled patch is already in this route.");
			return;
		}
		JList<FarmRunPatch> candidates = new JList<>(available.toArray(new FarmRunPatch[0]));
		candidates.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		candidates.setVisibleRowCount(Math.min(12, available.size()));
		candidates.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override public Component getListCellRendererComponent(JList<?> l, Object value, int index,
				boolean selected, boolean focus)
			{
				FarmRunPatch patch = (FarmRunPatch) value;
				boolean grouped = availableDraft.groups.containsKey(FarmRouteManager.key(patch));
				return super.getListCellRendererComponent(l,
					(grouped ? patch.getLocation()
						: patch.getDisplayName() + " — " + patch.getPatchType().getDisplayName()),
					index, selected, focus);
			}
		});
		JScrollPane scroll = new JScrollPane(candidates);
		FarmingPatchPanel.styleNarrowScrollBar(scroll);
		int result = JOptionPane.showConfirmDialog(parent, scroll, "Add patches to custom route",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result != JOptionPane.OK_OPTION) { return; }
		for (FarmRunPatch patch : candidates.getSelectedValuesList())
		{
			model.addElement(patch);
			String key = FarmRouteManager.key(patch);
			choices.put(key, availableDraft.choices.getOrDefault(key, RouteTeleport.NONE));
			if (availableDraft.groups.containsKey(key))
			{
				groups.put(key, new ArrayList<>(availableDraft.groups.get(key)));
			}
		}
		if (!candidates.isSelectionEmpty())
		{
			order.setSelectedItem("Custom");
			route.setSelectedIndex(model.size() - 1);
		}
	}

	private static void move(DefaultListModel<FarmRunPatch> model, JList<FarmRunPatch> list,
		int target, JComboBox<String> order)
	{
		int source = list.getSelectedIndex();
		if (source < 0 || target < 0 || target >= model.size() || source == target) { return; }
		FarmRunPatch p = model.remove(source);
		model.add(target, p);
		list.setSelectedIndex(target);
		order.setSelectedItem("Custom");
	}
}
