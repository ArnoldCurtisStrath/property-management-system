# The problem statement

## Theme

Real Estate and Property Management.

## 1. What problem exists?

Small rental agents and house managers in Kenya run several buildings for
several owners at the same time. Rent arrives in cash, M-Pesa and bank
transfers, often in pieces and often late, through a caretaker. Expenses for
water, security, garbage and repairs are scattered across WhatsApp, receipts
and memory. At the end of the month the agent has to answer three questions:
who owes what, where did the money go, and what should the owner be paid.

The records do not agree. The notebook, the M-Pesa messages and the tenant's
memory tell different stories. The agent cannot prove what was collected, the
owner cannot verify the statement, the caretaker is accused of a shortfall,
and the tenant has no receipt. Existing property apps digitise the
landlord's notebook; they still assume one full monthly payment and one person
holding the truth.

## 2. Who experiences the problem?

The primary user is the **rental agent or house manager** with roughly 10 to
200 units across several buildings, managing for more than one owner, with one
or two caretakers. The people around the agent feel the same problem:

- **The owner** wants a statement that shows rent collected, expenses, the
  agent's fee and the net remittance.
- **The caretaker** wants to hand over the day's cash without being blamed
  for a difference he cannot explain.
- **The tenant** wants proof of what was paid, especially when rent is paid in
  parts or through someone else.

## 3. Why is the problem important?

Rent is the agent's income and the owner's return. A forgotten cash payment,
an unrecorded M-Pesa transfer, a repair that was paid twice or a shortfall at
handover is money lost. It is also trust lost: owners change agents, tenants
are evicted over disputed payments, and good caretakers are dismissed because
there is no record.

The informal rental market runs on cash and mobile money. It needs a record
that works in that reality, not one that assumes a bank transfer and a
desktop.

## 4. How is the problem currently handled?

With a duplicate receipt book, a notebook, a spreadsheet, WhatsApp and the
M-Pesa inbox. Each building may have its own file. At month end the agent
counts the book against the phone, writes the owner a summary by hand, and
tries to remember which repair was paid. A spreadsheet can hold the numbers
but it cannot issue a receipt, reconcile an M-Pesa message, close a caretaker's
day or produce a branded owner statement.

## 5. What are the limitations of the current approach?

- The notebook has one copy. It can be lost, changed or disputed.
- M-Pesa messages are not linked to a tenant, a month or a balance.
- Part payments and carried balances are not represented; "paid" is treated
  as a yes or no.
- There is no expense ledger, so the owner's net cannot be proved.
- There is no day close, so cash handed over by a caretaker cannot be checked.
- There is no ageing, so the oldest debt is invisible.
- There is no owner statement, so the agent looks disorganised even when the
  work was done.
- Most apps assume a single landlord, a full monthly payment and a bank
  transfer. They do not fit a caretaker-managed compound.

## 6. How does the app solve it?

Sahihi is a rent ledger for the agent. It keeps buildings, units, tenants,
payments, expenses and repairs in one Room database on the phone and derives
the answers from that one ledger.

- **Properties → units → tenants.** A building holds many units; a tenant
  holds one unit; every charge and payment hangs off that tenancy.
- **A real ledger.** Rent is charged per month from move-in to move-out,
  prorated for a mid-month start or end. Payments are applied to the oldest
  unpaid month first. The balance, the oldest unpaid month and the ageing
  bucket are computed from the entries.
- **Receipts.** Every payment gets a sequential receipt number, a balance
  after it, a collector and a reference. The receipt is generated as an image
  and shared through WhatsApp.
- **M-Pesa reconciliation.** The agent pastes the confirmation message; the
  app reads the amount, phone, date and reference, matches the phone to a
  tenant and shows the balance before the payment is saved.
- **Expenses and owner statements.** Water, security, garbage, repairs and
  other costs are recorded against a building. The statement shows collected
  rent, expenses by category, the management fee and the net to the owner.
- **Day close.** The cash and M-Pesa collected today, the receipts issued and
  the cash handed over are compared; the variance is visible.
- **Repairs.** A request has a priority, a status and a cost. A completed
  repair with a cost becomes an expense, so the owner statement includes it.
- **Visit planner.** Open-Meteo gives the current weather and a three-day rain
  forecast for the building, so the agent can plan a visit around an open roof
  repair.
- **Daily brief.** WorkManager sends a 7am notification with the month's
  collected/expected, the arrears and the open repairs, even when the app is
  closed.
- **Offline first.** Everything except the weather works without a network.

The product is not a digitised notebook. It is the reconciliation layer
between the agent, the owner, the caretaker and the tenant. The receipt is the
tenant's copy, the day close is the caretaker's copy, the statement is the
owner's copy, and the ledger is the agent's copy. They all come from the same
entries.

## 7. What is deliberately out of scope for version 2.0?

- Cloud sync and multiple phones. The ledger is offline and single-device.
- Reading SMS automatically. The agent pastes the message; the app does not
  ask for the SMS permission.
- Photo evidence on repairs.
- A tenant portal.
- Recurring expense templates and lease-expiry reminders.

These are the next improvements, not reasons to make the core ledger heavier.
