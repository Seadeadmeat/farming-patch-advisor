# Version history

## V1.7 — 2026-09-18

- Fixed the Civitas illa Fortis patch changing from `READY` to `DEAD` when
  harvesting began. A temporary `Clear` action during a confirmed harvest now
  remains a live harvest transition.
- Explicit dead and diseased states still take priority, and completed timers
  are removed only after the patch object becomes genuinely empty.
- Refreshes the pending harvest state on every harvest or pick action so
  multi-pick crops continue to be evaluated from their latest object state.

## V1.6 — 2026-09-18

- Audited every protection payment used by the plugin against the current Old
  School RuneScape Wiki, including allotments, hops, bushes, trees, fruit trees,
  hardwoods, special patches, cacti, seaweed, and coral. All 51 existing payment
  item IDs and quantities were correct.
- Updated payment labels to use exact in-game item names where the checklist had
  used informal plurals, such as `Coconut`, `Watermelon`, and `Ground tooth`.
- Added an exhaustive regression test for every payable crop in the catalog and
  for crops that correctly have no gardener protection payment. Grape-vine
  saltpetre remains a planting requirement, not a protection payment.

## V1.5 — 2026-09-18

- Added **New custom route**, **Add patches**, and **Remove selected** controls
  to **Route & Teleports**. Players can build a route from enabled patches in the
  selected run, then arrange them by dragging or with the move buttons.
- Limited each patch's teleport dropdown to travel choices that reasonably serve
  that patch's area. `None / no item needed` remains available everywhere.
- Invalid older teleport selections safely fall back to no required item, while
  disabled route stops retain their saved position and membership.

## V1.4 — 2026-09-18

- Fixed living, harvestable cactus and potato cactus patches being marked dead
  when their object actions include both a harvest option and `Clear`.
- Explicitly dead plants and non-harvestable plants whose only applicable patch
  action is `Clear` continue to use the red `DEAD` state.

## V1.3 — 2026-09-17

- Corrected all five hardwood growth timers to match RuneLite's 640-minute
  growth cycles: teak 4,480 minutes; mahogany, camphor, and ironwood 5,120;
  rosewood 5,760. Inspected stage estimates now use the same cycle length.
- Added checks for planted and inspected stage calculations for every hardwood.
- Existing saved hardwood timers using the old estimates are corrected when that
  character's timers load; unrelated timer values are left intact.
- These are maximum estimates until a character's growth-tick offset is known;
  RuneLite Time Tracking can predict a more precise completion time.

## V1.2 — 2026-09-17

- Sidebar patch and contract cards now grow to fit wrapped text instead of relying
  on fixed heights, including long names, payment details, timers, and teleports.
- Compact header buttons fit the RuneLite sidebar; the route editor adapts its
  scroll area to the available screen and wraps long stop names.
- Local visual polish pending in-game confirmation.

## V1.1 — 2026-09-17

- Added a Route & Teleports editor with drag ordering, up/down controls, default
  order, Save, and Cancel; preferences belong to each character and run selection.
- Custom routes follow enabled patches and locations, with matching panel and
  ground-label patch numbers. Disabled stops retain saved choices.
- Added teleport checklist sections, equipped-item detection, and bank filter/highlight
  support for chosen supplies. Reusable items are deduplicated and tablets count per visit.
- Local update; in-game verification and publication are pending.

## V1.0 — 2026-09-17

Established the current local plugin as the V1.0 baseline, including the existing
Morytania patch-matching fix, performance optimizations, and harvest-status changes.
These local changes still require in-game confirmation and publication.

Teleport checklist and custom route ordering are preview concepts and are not
included in V1.0. The next update will be V1.1, followed by V1.2, V1.3, and so on.
