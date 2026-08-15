# Farming Patch Advisor

A RuneLite farm-run assistant that recommends crops, tracks patch growth, and prepares the items needed for configurable farming routes.

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
- The side panel has a one-click checklist control and per-patch right-click reset actions. Settings can include or exclude individual patch types, tools, compost, and protection payments.
- Adds a `Farm Run` button above the bank incinerator controls that filters the bank to the current checklist.
- Supports allotment, flower, herb, hops, bush, tree, fruit tree, hardwood, cactus, mushroom, belladonna, calquat, spirit tree, seaweed, grapevine, celastrus, redwood, Hespori, crystal tree, and coral patches.
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
