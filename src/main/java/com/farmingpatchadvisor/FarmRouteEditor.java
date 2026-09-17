package com.farmingpatchadvisor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
		List<FarmRunPatch> active = routes.activePatches();
		if (active.isEmpty())
		{
			JOptionPane.showMessageDialog(parent, "Enable patches and locations in settings first.");
			return;
		}
		DefaultListModel<FarmRunPatch> model = new DefaultListModel<>();
		active.forEach(model::addElement);
		Map<String, RouteTeleport> choices = new LinkedHashMap<>();
		active.forEach(p -> choices.put(FarmRouteManager.key(p), routes.teleport(run, p)));
		JList<FarmRunPatch> list = new JList<>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		list.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override public Component getListCellRendererComponent(JList<?> l, Object value, int index,
				boolean selected, boolean focus)
			{
				FarmRunPatch p = (FarmRunPatch) value;
				return super.getListCellRendererComponent(l,
					(index + 1) + ". " + p.getDisplayName() + " — " + p.getPatchType().getDisplayName(),
					index, selected, focus);
			}
		});
		JComboBox<String> order = new JComboBox<>(new String[]{"Default", "Custom"});
		order.setSelectedItem(routes.isCustom(run) ? "Custom" : "Default");
		JComboBox<RouteTeleport> teleport = new JComboBox<>(RouteTeleport.values());
		list.addListSelectionListener(e ->
		{
			FarmRunPatch p = list.getSelectedValue();
			if (p != null) { teleport.setSelectedItem(choices.get(FarmRouteManager.key(p))); }
		});
		teleport.addActionListener(e ->
		{
			FarmRunPatch p = list.getSelectedValue();
			if (p != null) { choices.put(FarmRouteManager.key(p), (RouteTeleport) teleport.getSelectedItem()); }
		});
		order.addActionListener(e ->
		{
			if ("Default".equals(order.getSelectedItem()))
			{
				List<FarmRunPatch> defaults = new ArrayList<>(FarmRunCatalog.patches());
				defaults.removeIf(p -> !choices.containsKey(FarmRouteManager.key(p)));
				defaults = FarmRouteManager.order(defaults, java.util.Collections.emptyList());
				model.clear();
				defaults.forEach(model::addElement);
				list.setSelectedIndex(0);
			}
		});
		JButton up = new JButton("Move up");
		JButton down = new JButton("Move down");
		up.addActionListener(e -> move(model, list, list.getSelectedIndex() - 1, order));
		down.addActionListener(e -> move(model, list, list.getSelectedIndex() + 1, order));
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
		header.add(new JLabel("Route order — " + run));
		header.add(order);
		header.add(new JLabel("Drag stops or use Move up / Move down."));
		JPanel controls = new JPanel(new GridLayout(0, 1, 0, 6));
		JPanel arrows = new JPanel(new GridLayout(1, 2, 6, 0));
		arrows.add(up); arrows.add(down); controls.add(arrows);
		controls.add(new JLabel("Teleport for selected patch:"));
		controls.add(teleport);
		controls.add(new JLabel("Choose travel you have unlocked. Check daily/remaining charges."));
		JPanel editor = new JPanel(new BorderLayout(0, 8));
		editor.add(header, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(list);
		FarmingPatchPanel.styleNarrowScrollBar(scroll);
		scroll.setPreferredSize(new Dimension(460, 360));
		editor.add(scroll, BorderLayout.CENTER);
		editor.add(controls, BorderLayout.SOUTH);
		list.setSelectedIndex(0);
		int result = JOptionPane.showOptionDialog(parent, editor, "Farm Run — Route & Teleports",
			JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
			new String[]{"Save route", "Cancel"}, "Save route");
		if (result == 0)
		{
			List<FarmRunPatch> ordered = new ArrayList<>();
			for (int i = 0; i < model.size(); i++) { ordered.add(model.get(i)); }
			if (!routes.save(profile, run, ordered, choices, "Custom".equals(order.getSelectedItem())))
			{
				JOptionPane.showMessageDialog(parent, "Character changed. Reopen the editor to save this character's route.");
			}
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
