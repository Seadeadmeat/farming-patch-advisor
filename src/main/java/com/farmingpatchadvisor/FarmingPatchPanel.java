package com.farmingpatchadvisor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.JToggleButton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

final class FarmingPatchPanel extends PluginPanel
{
	private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());
	private final PatchTimerManager timerManager;
	private final FarmingPatchAdvisorConfig config;
	private final ConfigManager configManager;
	private final FarmingContractManager contractManager;
	private final FarmingLoadout farmingLoadout;
	private final JToggleButton checklistToggle = new JToggleButton();
	private final JPanel patches = new JPanel();
	private final Timer refreshTimer;

	@Inject
	private FarmingPatchPanel(PatchTimerManager timerManager, FarmingPatchAdvisorConfig config,
		ConfigManager configManager, FarmingContractManager contractManager, FarmingLoadout farmingLoadout)
	{
		this.timerManager = timerManager;
		this.config = config;
		this.configManager = configManager;
		this.contractManager = contractManager;
		this.farmingLoadout = farmingLoadout;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
		JLabel title = new JLabel("Farm Run");
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
		title.setForeground(Color.WHITE);
		header.add(title);
		header.add(Box.createRigidArea(new Dimension(0, 12)));
		JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
		actions.setAlignmentX(Component.LEFT_ALIGNMENT);
		actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		actions.setOpaque(false);
		styleButton(checklistToggle);
		checklistToggle.addActionListener(event -> toggleChecklist());
		updateChecklistButton();
		actions.add(checklistToggle);
		JButton clear = new JButton("Clear timers");
		styleButton(clear);
		clear.addActionListener(event -> timerManager.clear());
		actions.add(clear);
		header.add(actions);
		add(header, BorderLayout.NORTH);

		patches.setLayout(new BoxLayout(patches, BoxLayout.Y_AXIS));
		patches.setBackground(ColorScheme.DARK_GRAY_COLOR);
		patches.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		add(patches, BorderLayout.CENTER);

		refreshTimer = new Timer(1000, event -> rebuild());
		refreshTimer.start();
		rebuild();
	}

	void stop()
	{
		refreshTimer.stop();
	}

	void start()
	{
		if (!refreshTimer.isRunning())
		{
			refreshTimer.start();
		}
		rebuild();
	}

	private void rebuild()
	{
		updateChecklistButton();
		patches.removeAll();
		if (config.showFarmingContract())
		{
			patches.add(createContractCard());
			patches.add(Box.createRigidArea(new Dimension(0, 10)));
		}
		Set<ChecklistPatch> selectedPatches = ChecklistPatch.selected(config);
		List<PatchTimer> unmatchedTimers = new ArrayList<>(timerManager.getTimers());
		unmatchedTimers.removeIf(timer -> !ChecklistPatch.includes(selectedPatches, timer.getPatchType()));
		int order = 1;
		List<FarmRunPatch> enabledCatalog = FarmRunCatalog.patches(config);
		for (FarmRunType runType : FarmRunType.values())
		{
			boolean runSelected = enabledCatalog.stream().anyMatch(patch -> patch.getFarmRunType() == runType
				&& ChecklistPatch.includes(selectedPatches, patch.getPatchType()));
			if (!runSelected)
			{
				continue;
			}
			patches.add(createRunHeader(runType));
			patches.add(Box.createRigidArea(new Dimension(0, 5)));
			for (FarmRunPatch patch : enabledCatalog)
			{
				if (patch.getFarmRunType() != runType
					|| !ChecklistPatch.includes(selectedPatches, patch.getPatchType()))
				{
					continue;
				}
				List<PatchTimer> matchingTimers = takeMatchingTimers(unmatchedTimers, patch);
				patches.add(matchingTimers.isEmpty()
					? createUntrackedCard(order++, patch)
					: createTrackedCard(order++, patch, matchingTimers));
				patches.add(Box.createRigidArea(new Dimension(0, 5)));
			}
			for (int i = unmatchedTimers.size() - 1; i >= 0; i--)
			{
				PatchTimer timer = unmatchedTimers.get(i);
				if (FarmRunType.forPatchType(timer.getPatchType()) != runType)
				{
					continue;
				}
				unmatchedTimers.remove(i);
				FarmRunPatch unknown = new FarmRunPatch(PatchLocationCatalog.name(timer.getPatchLocation()), "", timer.getPatchType());
				patches.add(createTrackedCard(order++, unknown, java.util.Collections.singletonList(timer)));
				patches.add(Box.createRigidArea(new Dimension(0, 5)));
			}
			patches.add(Box.createRigidArea(new Dimension(0, 7)));
		}
		patches.revalidate();
		patches.repaint();
	}

	private JPanel createContractCard()
	{
		FarmingContract contract = contractManager.getContract();
		if (contract == null)
		{
			JPanel card = createCard(66, false, ColorScheme.MEDIUM_GRAY_COLOR);
			addLine(card, "Farming Contract", new Color(255, 152, 31), true);
			addLine(card, "Contract unknown", Color.LIGHT_GRAY, true);
			addLine(card, "Talk to Guildmaster Jane", Color.GRAY, false);
			return card;
		}

		Crop crop = contract.getCrop();
		List<ProtectionPayment> payments = ProtectionPaymentCatalog.forCrop(crop);
		PatchTimer timer = findContractTimer(crop);
		int lineCount = 5 + Math.max(1, payments.size())
			+ (timer != null && (timer.isDead() || timer.isDiseased()) ? 1 : 0);
		JPanel card = createCard(20 + lineCount * 17, false, ColorScheme.MEDIUM_GRAY_COLOR);
		addLine(card, "Farming Contract", new Color(255, 152, 31), true);
		addLine(card, "Crop: " + contract.getName(), Color.WHITE, true);
		addLine(card, "Patch: " + crop.getPatchType().getDisplayName() + " - Farming Guild",
			Color.LIGHT_GRAY, false);
		addLine(card, "Seed: " + farmingLoadout.inventoryQuantity(crop.getItemId()) + "/"
			+ crop.getQuantity() + " " + crop.getName(), config.seedColor(), false);
		if (payments.isEmpty())
		{
			addLine(card, "Payment: None", Color.GRAY, false);
		}
		else
		{
			for (ProtectionPayment payment : payments)
			{
				addLine(card, "Payment: " + farmingLoadout.inventoryQuantity(payment.getItemId()) + "/"
					+ payment.getQuantity() + " " + payment.getName(), config.requiredItemColor(), false);
			}
		}
		if (timer == null)
		{
			addLine(card, "Status: Not tracked", Color.GRAY, false);
		}
		else
		{
			Instant now = Instant.now();
			boolean ready = !timer.isDead() && !timer.isDiseased()
				&& !now.isBefore(timer.getReadyAt());
			addLine(card, "Status: " + (timer.isDead() ? "DEAD" : timer.isDiseased() ? "DISEASED"
				: ready ? "READY"
				: PatchTimerOverlay.formatRemaining(Duration.between(now, timer.getReadyAt()))),
				timer.isDead() ? Color.RED : timer.isDiseased() ? Color.YELLOW
					: ready ? Color.RED : Color.GREEN, true);
			String remedy = PatchRemedy.forTimer(timer);
			if (remedy != null)
			{
				addLine(card, "Remedy: " + remedy, Color.LIGHT_GRAY, false);
			}
		}
		return card;
	}

	private PatchTimer findContractTimer(Crop crop)
	{
		for (PatchTimer timer : timerManager.getTimers())
		{
			if (timer.getPatchType() == crop.getPatchType() && timer.getCrop().equals(crop)
				&& "Farming Guild".equals(PatchLocationCatalog.name(timer.getPatchLocation())))
			{
				return timer;
			}
		}
		return null;
	}

	private static JPanel createRunHeader(FarmRunType runType)
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.setPreferredSize(new Dimension(PANEL_WIDTH - 28, 30));
		header.setMinimumSize(new Dimension(PANEL_WIDTH - 28, 30));
		header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
		header.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		JLabel label = new JLabel(runType.getDisplayName());
		label.setForeground(Color.WHITE);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		header.add(label, BorderLayout.WEST);
		return header;
	}

	private void toggleChecklist()
	{
		configManager.setConfiguration("farming-patch-advisor", "showChecklistOverlay",
			!config.showChecklistOverlay());
		updateChecklistButton();
	}

	private void updateChecklistButton()
	{
		boolean selected = config.showChecklistOverlay();
		checklistToggle.setSelected(selected);
		checklistToggle.setText(selected ? "Checklist ON" : "Checklist OFF");
		checklistToggle.setBackground(selected ? new Color(45, 110, 70) : ColorScheme.MEDIUM_GRAY_COLOR);
		checklistToggle.setForeground(Color.WHITE);
	}

	private static void styleButton(AbstractButton button)
	{
		button.setFocusPainted(false);
		button.setForeground(Color.WHITE);
		button.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ColorScheme.LIGHT_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 8, 6, 8)));
	}

	private static List<PatchTimer> takeMatchingTimers(List<PatchTimer> timers, FarmRunPatch patch)
	{
		List<PatchTimer> matching = new ArrayList<>();
		for (int i = timers.size() - 1; i >= 0; i--)
		{
			PatchTimer timer = timers.get(i);
			if (timer.getPatchType() == patch.getPatchType()
				&& PatchLocationCatalog.name(timer.getPatchLocation()).equals(patch.getLocation()))
			{
				matching.add(0, timers.remove(i));
			}
		}
		return matching;
	}

	private JPanel createUntrackedCard(int order, FarmRunPatch patch)
	{
		JPanel card = createCard(48, false, ColorScheme.MEDIUM_GRAY_COLOR);
		addLine(card, order + ". " + patch.getDisplayName(), Color.WHITE, true);
		addLine(card, patch.getPatchType().getDisplayName() + " - Not inspected", Color.GRAY, false);
		addResetMenu(card, patch);
		return card;
	}

	private JPanel createTrackedCard(int order, FarmRunPatch patch, List<PatchTimer> matchingTimers)
	{
		Instant now = Instant.now();
		boolean ready = matchingTimers.stream().anyMatch(timer -> timer.isDead() || timer.isDiseased()
			|| !now.isBefore(timer.getReadyAt()));
		Color readyColor = (System.currentTimeMillis() / 500L) % 2 == 0 ? Color.RED : new Color(140, 0, 0);
		int height = 48 + matchingTimers.size() * 57;
		for (PatchTimer timer : matchingTimers)
		{
			if ((!timer.isPlantedTimer() && !timer.isDead() && !timer.isDiseased())
				|| timer.isDead() || timer.isDiseased())
			{
				height += 18;
			}
		}
		JPanel card = createCard(height, ready, readyColor);
		addLine(card, order + ". " + patch.getDisplayName(), ready ? readyColor : Color.WHITE, true);
		addLine(card, "Patch: " + patch.getPatchType().getDisplayName()
			+ (patch.getPatchCount() > 1 ? " (" + matchingTimers.size() + "/" + patch.getPatchCount() + " tracked)" : ""),
			Color.LIGHT_GRAY, false);
		int timerNumber = 1;
		for (PatchTimer timer : matchingTimers)
		{
			String prefix = matchingTimers.size() > 1 ? timerNumber++ + ". " : "";
			addLine(card, prefix + "Planted: " + timer.getCrop().getName() + " x" + timer.getCrop().getQuantity(), Color.LIGHT_GRAY, false);
			addLine(card, (timer.isPlantedTimer() ? "Started: " : "Inspected: ") + CLOCK.format(timer.getPlantedAt()), Color.LIGHT_GRAY, false);
			if (!timer.isPlantedTimer() && !timer.isDead() && !timer.isDiseased())
			{
				addLine(card, "Stage: " + timer.getEstimatedStage(now) + "/" + timer.getTotalStages() + " (maximum estimate)", Color.LIGHT_GRAY, false);
			}
			boolean timerReady = !timer.isDead() && !timer.isDiseased()
				&& !now.isBefore(timer.getReadyAt());
			String remaining = timer.isDead() ? "DEAD" : timer.isDiseased() ? "DISEASED"
				: timerReady ? "READY"
				: PatchTimerOverlay.formatRemaining(Duration.between(now, timer.getReadyAt()));
			addLine(card, "Time remaining: " + remaining,
				timer.isDead() ? Color.RED : timer.isDiseased() ? Color.YELLOW
					: timerReady ? readyColor : Color.GREEN, true);
			String remedy = PatchRemedy.forTimer(timer);
			if (remedy != null)
			{
				addLine(card, "Remedy: " + remedy, Color.LIGHT_GRAY, false);
			}
		}
		addResetMenu(card, patch);
		return card;
	}

	private void addResetMenu(JPanel card, FarmRunPatch patch)
	{
		JPopupMenu menu = new JPopupMenu();
		JMenuItem reset = new JMenuItem("Reset " + patch.getDisplayName());
		reset.addActionListener(event -> timerManager.reset(patch.getLocation(), patch.getPatchType()));
		menu.add(reset);
		card.setComponentPopupMenu(menu);
		for (Component component : card.getComponents())
		{
			if (component instanceof JLabel)
			{
				((JLabel) component).setComponentPopupMenu(menu);
			}
		}
	}

	private static JPanel createCard(int height, boolean ready, Color readyColor)
	{
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setAlignmentX(Component.LEFT_ALIGNMENT);
		card.setPreferredSize(new Dimension(PANEL_WIDTH - 28, height));
		card.setMinimumSize(new Dimension(PANEL_WIDTH - 28, height));
		card.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(ready ? readyColor : ColorScheme.MEDIUM_GRAY_COLOR),
			BorderFactory.createEmptyBorder(6, 8, 6, 8)));
		return card;
	}

	private static void addLine(JPanel card, String text, Color color, boolean bold)
	{
		JLabel label = new JLabel(text);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		label.setHorizontalAlignment(JLabel.LEFT);
		label.setForeground(color);
		if (bold)
		{
			label.setFont(label.getFont().deriveFont(Font.BOLD));
		}
		card.add(label);
	}
}
