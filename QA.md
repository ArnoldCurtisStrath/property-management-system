# Sahihi — technical defence cheat sheet

Short answers for the questions the lecturer is likely to ask. Point at the
file when you answer; do not recite.

## Architecture

**1. Why this architecture?**
The screen shows and passes on input. The ViewModel holds state. The
repository is the only place that knows where data comes from. Room is the
local source, Open-Meteo the remote one. We can change a screen without
touching the database, and test the ledger without Android.
Files: `ui/*`, `data/repository/*`, `AppContainer.kt`.

**2. How does data move from a tap to the database?**
Fragment → ViewModel → repository → DAO → Room. A LiveData/Mediator chain
brings the changed rows back to the screen.
Example: `CollectSheet` → `LedgerRepository.recordPayment` → `PaymentDao`.

**3. Why is `DashboardViewModel` not talking to a DAO?**
It used to. In v2 it uses `LedgerRepository`; no ViewModel opens
`AppDatabase` directly. That is the separation the brief asks for.

**4. What is the `AppContainer`?**
A small service locator in `SahihiApp`. It builds the database, repositories
and settings once so every screen gets the same objects.

## Room and the ledger

**5. What is stored, and what is derived?**
Stored: buildings, units, tenants, payments, expenses, repairs.
Derived: rent charges, balance, oldest unpaid month, ageing, collection rate,
statement. `LedgerCalculator` does the arithmetic.

**6. How is a balance calculated?**
Charges from the start month to the current month (prorated for a mid-month
start or end), minus paid payments. Payments are allocated to the oldest
unpaid charge first; an excess is a credit.

**7. How does the migration work?**
`AppDatabase` is version 2. `MIGRATION_1_2` creates `buildings`, adds the new
columns, backfills the old properties into one building, numbers old receipts
and creates `expenses`. It does not delete old rows.

**8. Why store `balanceAfter` on a payment?**
A receipt is a snapshot. If a later payment changes the live balance, a
re-printed receipt must still show what the tenant saw at the time.

**9. Why is the receipt number sequential?**
A gap is visible. The number is the payment id formatted with the year and a
prefix, e.g. `SH-2026-0010`.

**10. What happens when a payment is deleted?**
The row is deleted; the ledger recomputes; the receipt number is not reused
because it is based on the payment id. The tenant's copy remains their copy.

## M-Pesa

**11. How does the parser work?**
`MpesaParser` uses regex for amount, phone, date and reference. It normalises
the phone to the last nine digits, then the screen matches it to an active
tenant. The agent confirms before saving. Unit tests cover several formats.

**12. Why not read SMS automatically?**
Android restricts SMS access and users should not grant it for a rent app.
Paste is deliberate, works with any phone, and keeps the privacy boundary.

**13. What if a message is not understood?**
The preview is not shown and a message explains. A failed parse never writes.

## API and background

**14. Why Open-Meteo?**
It is free, keyless and reliable. The app geocodes the building address, then
asks for current weather and a three-day rain forecast. That is meaningful:
rain next to open roof repairs.

**15. What if the API is down?**
The card shows "Weather unavailable" and a Retry button. The ledger does not
depend on it.

**16. Why WorkManager?**
The daily brief must run when the app is closed and after a reboot. A periodic
request with a unique name survives both. It is not a timer in an Activity.

**17. How does the daily brief get its numbers?**
`RentReminderWorker` calls `LedgerRepository.snapshotToday()` and
`NotificationHelper.showDailyBrief`. The same calculation as the Today screen,
so the notification cannot disagree with the app.

## UI and proof

**18. How is the receipt shared?**
`ReceiptGenerator` draws a PNG with Canvas, writes it to the cache, exposes it
through `FileProvider`, and sends a scoped URI in an `ACTION_SEND` chooser.
No file path is exposed.

**19. How is the owner statement different from the ledger?**
The ledger is the tenant's account. The statement is the building's month:
collected, expenses, management fee, net to owner, arrears and vacancy. It is
the agent's client document.

**20. What would you improve?**
Sync for caretaker and owner phones, photo evidence on repairs, lease-expiry
reminders, recurring expenses, and a tenant portal. The ledger core is ready
for them.

## If you get stuck

Say: *"The code is in [file]; I can show the exact function after the
presentation."* Then point at the relevant screen. Do not invent a file name.
