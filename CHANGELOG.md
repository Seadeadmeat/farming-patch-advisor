# Version history

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
