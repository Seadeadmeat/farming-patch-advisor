# Farming Patch Advisor

Plan and track efficient farm runs with patch highlights, persistent growth timers, customizable run checklists, bank and Seed Vault support, farming contract assistance, crop recommendations, protection payments, and patch-care reminders.

## How to use

1. **Configure settings.** Choose a **Seed recommendation**, set any **Crop overrides**, select the patch types for each run, and disable locked areas under **Patch locations**.
2. **Set up overlays.** Enable **Show timer overlay** and **Show run checklist**, then hold `Alt` in RuneLite to move them. Enable **Bank-side checklist** and choose its side and height for a compact checklist beside the bank or Seed Vault.
3. **Scan your storage.** Open your bank and the Farming Guild Seed Vault once so recommendations and checklist quantities reflect the seeds, saplings, tools, compost, and payments you own.
4. **Choose a run.** Open the **Farm Run** side panel and select a run from the dropdown. Patch highlights, timers, checklists, and the bank filter follow that selection.
5. **Plant and inspect.** Follow the highlighted patches and checklist. Planting starts persistent timers, while inspecting an existing crop updates its crop, stage, health, and estimated time remaining.
6. **Care for patches.** Follow the compost and water reminders above supported patches. Diseased and dead patches receive colored outlines and remedy guidance.
7. **Use the bank filter.** Open a bank or the Seed Vault and select **Farm Run Filter** to show only the items needed for the selected run and active Farming Guild contract.

## Features

- Labels recognized empty or weedy farming patches with one outline around the complete patch and displays the recommended crop, quantity, and Farming level.
- Highlights recommended seeds and saplings in the bank, inventory, and Farming Guild seed vault.
- Selects either the highest available crop or the highest crop unlocked by the player's Farming level.
- Provides a crop override dropdown for every patch category, allowing choices such as Ranarr instead of the automatic herb recommendation.
- Highlights the standard planting kit (rake, seed dibber, spade, and optionally ultracompost) in blue.
- Starts a persistent per-patch countdown when a seed or sapling is used on a patch. The movable timer overlay pulses red when a crop is ready and clears harvested patches.
- Adds a Farm Run side panel with patch location, patch type, planted crop, planting time, growth stage, and live countdown, grouped into efficient run categories.
- Inspecting an already-planted patch reads its `x/x` growth stage and creates a conservative maximum-time estimate for that stage. A timer captured at planting always takes precedence.
- Automatically advances inspected growth-stage estimates as time elapses.
- Provides a movable, configurable full-run checklist using `inventory / still needed` quantities. Planting reduces the required seed or sapling count, and reusable tools disappear once they are in the inventory.
- Includes exact gardener protection payments, including filled sacks and baskets, and counts noted payment items through RuneLite's item-variation mapping.
- The side panel has a one-click checklist control and per-patch right-click reset actions. Settings can include or exclude individual patch types, inaccessible locations, tools, compost, and protection payments.
- Tracks the current Farming Guild contract from Guildmaster Jane (with RuneLite Time Tracking fallback), shows its crop, patch, seed, payment, inventory count, and timer in the Farm Run panel, and highlights the matching guild patch and required items.
- Adds a `Farm Run` button above the bank incinerator controls that filters the bank to the current checklist.
- Shows a compact missing-items checklist beside the bank or seed vault, with configurable side and vertical alignment, while temporarily hiding the primary movable checklist.
- Supports allotment, flower, herb, hops, bush, tree, fruit tree, hardwood, cactus, mushroom, belladonna, calquat, spirit tree, seaweed, grapevine, celastrus, redwood, Hespori, anima, crystal tree, and coral patches.
- Works offline and uses RuneLite `gameval` item constants rather than hard-coded item IDs.

Automatic recommendations do not attempt to optimize Grand Exchange profit. Use the per-category crop overrides when a specific crop is preferred.

## Development

Requires Java 11.

```text
./gradlew test
./gradlew run
```

For Jagex-account login in the development client, follow RuneLite's [Using Jagex Accounts](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts) guide.

## License

BSD 2-Clause. See `LICENSE`.
