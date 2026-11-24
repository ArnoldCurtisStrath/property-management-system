# Sahihi 2.1 — release notes

**Every shilling, accounted for.**

Sahihi 2.1 is a design release. The ledger, the flows and the database are the
same as 2.0; the app now looks and behaves like the rent book it always was.

## What changed in 2.1

- **The rent book.** Paper `#F4F1E9`, ink `#171A17` and one stamp red
  `#8A2E2A` for money still out. Gold, forest and burnt orange are gone.
- **Today is a statement, not a dashboard.** The gradient hero and the three
  "Do next" cards are replaced by a ruled month statement, a ranked
  **Needs you** list and today's receipt feed.
- **Rows instead of cards.** Buildings, Money, People and More are hairline
  rules and aligned figures, with 4–6dp corners and no elevation.
- **A real ledger.** The tenant book reads as `date · entry · running balance`;
  Money reads as `collected · expenses · net`, with arrears stamped.
- **The stamp is the status.** `OVERDUE`, `URGENT`, `VACANT` and the
  `SAHIHI VERIFIED` receipt stamp are the same mark.
- **The shell is fixed.** The bottom bar carries the gesture inset, the system
  bars are paper, and the black band and dead strip under the bar are gone.
- **Type.** DM Sans for the interface, DM Mono for every figure, DM Serif only
  for the wordmark and the receipt.
- **The shared receipt image** is regenerated in the new palette and stamp.

## The 2.0 feature set

This release turns the version 1 property manager into Sahihi, a rent ledger
for property agents. It is built on the same Room database and the same
repositories, not a rewrite.

### New

- Buildings → units → tenants hierarchy.
- Today screen with the month's run, who owes, vacancies and repairs.
- Tenant ledger: charges, part payments, running balance, ageing.
- Collect sheet with cash, M-Pesa and bank; sequential receipt numbers.
- Shareable receipt image with the **SAHIHI** stamp and balance after.
- M-Pesa paste reconciliation with amount, phone, date and reference matching.
- Expenses with categories, vendor and building.
- Owner statement: rent roll, collected, arrears, vacancy loss, expenses,
  management fee and net to owner.
- Day close with cash handed over and variance.
- Repairs with priority, status, cost and vendor; completed repairs create an
  expense.
- Visit planner with the current weather and a three-day rain forecast.
- Daily brief notification from WorkManager at 7am.
- Sample portfolio in More for demos and rehearsals.

## Database

- Room version 2.
- `Migration(1, 2)` adds buildings, unit/building links, tenant due day and
  deposit, payment receipt fields and expenses.
- Existing version 1 records are preserved and backfilled.

## Install

The signed APK is attached to the release. It installs over version 1.0 when
signed with the same key. Android 7 (API 24) or later.

## Known limits

- Single device, offline only; no sync.
- No photos on repairs.
- No lease-expiry reminders.
- The weather is the only feature that needs a network.

## Verification

- `gradlew.bat :app:testDebugUnitTest` passes.
- The signed release is installed and demonstrated on the emulator.
- The migration is tested by installing version 2 over a version 1 database.
