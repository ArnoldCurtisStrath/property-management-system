# Sahihi 2.0 — release notes

**Every shilling, accounted for.**

This release turns the version 1 property manager into Sahihi, a rent ledger
for property agents. It is built on the same Room database and the same
repositories, not a rewrite.

## New

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
- Brand and design system: paper and ink, DM Serif/Sans/Mono, receipt motif.
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
