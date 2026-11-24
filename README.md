# Sahihi

**Every shilling, accounted for.**

Sahihi is an Android rent ledger for the property agent who runs multiple
buildings for multiple owners. It turns every cash or M-Pesa payment into a
receipt, every receipt into a running balance, and every building into an
owner statement that can be shared on WhatsApp.

Built with Kotlin for the BBIT 3.2 Mobile Application Development CAT 2,
Strathmore University, 2026.

---

## The problem

Rent in Kenya's small rental market rarely moves in a straight line. It is
collected in cash and M-Pesa by caretakers, paid in pieces, and remembered
differently by the landlord, the caretaker and the tenant. At the end of the
month the notebook, the M-Pesa inbox and the tenant's memory disagree.

Existing property apps digitise the landlord's notebook. They still assume
one full monthly payment and one person holding the truth. The agent is left
with three jobs no single record can answer:

- **Who owes what, right now?** Not "who paid this month".
- **Where did the money go?** Rent in, expenses out, fee earned, net to owner.
- **Can I prove it?** To the tenant, the owner and the caretaker.

Sahihi is built around those three questions. The full problem statement is
in [`PROBLEM_STATEMENT.md`](PROBLEM_STATEMENT.md).

## What the app does

| Part | What it does |
|---|---|
| **Today** | The month's run: collected vs expected, who owes, vacant units, open repairs |
| **Buildings** | Buildings → units → tenants, occupancy, rent roll, expenses, net to owner |
| **Tenant ledger** | Charges, part payments, running balance, ageing, receipts, shareable statement |
| **Collect** | Cash, M-Pesa or bank; part payments; receipt number and balance after; shareable receipt |
| **M-Pesa reconciliation** | Paste a confirmation message; the app reads the amount, phone and date, matches the tenant, and posts the payment |
| **Money** | Cash in, cash out, net, receipts, expenses and arrears in one view |
| **Owner statement** | Rent roll, collected, arrears, vacancy loss, expenses by category, management fee, net |
| **Day close** | Cash/M-Pesa totals, receipts issued, cash handed over, variance |
| **Repairs** | Priority, status, cost, vendor; completed repairs create an expense |
| **Visit planner** | Weather forecast at the building; rain risk next to open repairs |
| **Daily brief** | A 7am WorkManager notification with the day's numbers |
| **Sample portfolio** | Three buildings, ten units, tenants, payments, expenses and repairs for demos |

The design goal is simple: **15 seconds from the tenant's door to a receipt**,
and **10 minutes to close the month**.

## The look

Sahihi is drawn as a working rent book, not a dashboard. Paper, ink and one
rubber stamp: red is reserved for money that is still out, green only for money
received. The month is a ruled statement, the ledger has columns and running
balances, receipts are stamped **SAHIHI VERIFIED**, and every figure is set in
a monospaced face so the columns line up. There are no gradient hero cards, no
KPI tiles and no card stacks; the bottom bar carries the gesture inset so the
paper runs edge to edge.

The palette is deliberately narrow: `#F4F1E9` paper, `#171A17` ink and
`#8A2E2A` stamp. DM Sans runs the interface, DM Mono carries the numbers, and
DM Serif signs the name.

## The four copies of the truth

The agent sits between four people who all need a different copy of the same
month:

| Who | Copy | What it protects |
|---|---|---|
| Tenant | Receipt | Proof of payment and the balance after it |
| Caretaker | Day close | A clean handover, with variance made visible |
| Owner | Statement | Rent collected, expenses, fee and net remittance |
| Agent | Ledger | The business: balances, arrears, occupancy and margin |

One ledger produces all four. That is the product.

## Screens

1. Sign in and create account
2. Today
3. Buildings list
4. Building details
5. Building form
6. Units list
7. Unit form / details
8. People (tenants)
9. Tenant form
10. Tenant ledger
11. Collect sheet
12. Receipt
13. Money
14. M-Pesa reconciliation
15. Owner statement
16. Day close
17. Repairs list / form
18. More / settings / about

## How it is built

```
Fragment / Activity
        ↓
ViewModel (screen state)
        ↓
Repository (LedgerRepository, StatementRepository, …)
        ↓
Room database  +  Open-Meteo API
```

- **Fragment** shows the screen and passes on what the user typed.
- **ViewModel** holds the state, so the screen survives rotation.
- **Repository** is the only place that knows where the data comes from.
- **Room** keeps buildings, units, tenants, payments, expenses and repairs.
- **LedgerCalculator** and **PortfolioCalculator** are pure Kotlin, unit-tested.
- **ReceiptGenerator** draws a real receipt and shares it through FileProvider.
- **MpesaParser** turns a pasted M-Pesa message into a payment preview.
- **DailyBriefWorker** uses WorkManager for the 7am brief.

The ledger is derived from the tenancy: rent × months from move-in to
move-out, prorated for a mid-month start or end. Payments are applied to the
oldest unpaid month first. The balance is the difference. Receipt numbers are
sequential, and the balance after a payment is stored so a reprinted receipt
never changes.

## Data model

| Table | What it holds |
|---|---|
| `buildings` | Building, address, owner, management fee |
| `properties` | Units: label, type, rent, status, building |
| `tenants` | Tenant, unit, rent, due day, deposit, start/end date |
| `payments` | Amount, date, month, method, receipt number, reference, balance after, collector |
| `expenses` | Building, category, amount, date, vendor, method, note |
| `maintenance` | Unit, title, category, priority, status, cost, vendor |

The database is at version 2. `MIGRATION_1_2` upgrades a version 1 database in
place, backfills the building, receipt numbers and timestamps, and keeps the
old records. The Room schema is exported to `app/schemas/`.

## Tools

- Kotlin, XML layouts, ViewBinding
- Room, DAOs, repositories, LiveData and ViewModel
- Navigation component and a bottom navigation bar
- RecyclerView with `ListAdapter` and `DiffUtil`
- WorkManager and notifications
- Coroutines
- `HttpURLConnection` and `JSONObject` for Open-Meteo
- `Canvas` and `FileProvider` for receipt images
- JUnit tests for the ledger, M-Pesa parser, money formatting and validation

## How to run

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run on a device or emulator running Android 7 (API 24) or later.
4. Create an account, then use **More → Load sample portfolio** for the demo
   data.

The app works offline. Only the visit planner needs the internet; if it is
unavailable the card says so and offers a retry.

## Tests

```powershell
gradlew.bat :app:testDebugUnitTest
```

The test suite covers:

- partial payments, overpayments, oldest-first allocation and ageing
- proration for a mid-month move-in
- M-Pesa message parsing and phone normalisation
- money formatting and form validation

## The team

| Name | Admission | Part |
|---|---|---|
| Arnold Wangai Mbici | 166569 | Lead, ledger, architecture and documentation |
| Kabutha Joe Mwangi | 172896 | Buildings, units and Today |
| Kwendo Andrew Abel | 192106 | Tenants, ledger and receipts |
| Lusiola Erika Wambui | 177886 | Payments, M-Pesa and owner statements |
| Leon Onyancha Ouya | 189367 | Repairs, weather and property pulse |
| Naaadir Abdulaziz Rana | 189548 | API, daily brief and release |

Version 1.0 of the project was built through the pull requests linked in the
repository history. Version 2.1 is the Sahihi rent book.

The repository is still called `property-management-system` for continuity
with the CAT 2 submission and the existing release links. The product inside
it is Sahihi.
