# Sahihi — 10 minute presentation

**Group 8 · BBIT Year 3.2 A · Mobile Application Development · 2026**

Every member speaks. The demo is live. The deck is only a backdrop.

## Before the room

- Install the signed `Sahihi 2.1` APK on the emulator or phone.
- Sign in once and leave the app on **Today**.
- If the app was used for testing, clear the app data and load the sample
  portfolio again, so Grace owes Ksh 4,000 and John owes Ksh 4,000.
- Keep the APK, a screen recording and this script on the presentation
  machine. The emulator is the primary; the APK is the backup.
- Notification permission must be allowed. Test **More → Send today's brief
  now** once before the room.

## The running order

| Time | Speaker | What |
|---|---|---|
| 0:00–0:40 | Arnold | Hook: three records that do not agree |
| 0:40–1:20 | Arnold | The agent, the owners and the four copies of the truth |
| 1:20–2:00 | Kabutha | Today: the month's run and the collection |
| 2:00–2:50 | Kwendo | Tenant ledger, part payment, receipt |
| 2:50–3:40 | Erika | M-Pesa paste, balance, receipt |
| 3:40–4:30 | Erika | Building statement: expenses, fee, net to owner |
| 4:30–5:10 | Leon | Repairs, cost and the rain forecast |
| 5:10–5:45 | Naaadir | Daily brief, API and WorkManager |
| 5:45–6:45 | Arnold | Architecture, ledger, migration and sharing |
| 6:45–8:15 | All | GitHub and what each person built |
| 8:15–9:15 | Arnold | Impact, roadmap and close |
| 9:15–10:00 | Buffer | Questions |

---

## Part 1 — The hook (Arnold, 0:00–1:20)

**Say:**

> Meet an agent in Nairobi. He manages three buildings for three owners.
> His office is a notebook, a WhatsApp inbox and a phone full of M-Pesa
> messages. At the end of the month, three records disagree: his notebook,
> the M-Pesa messages and the tenant's memory.
>
> The notebook is not the problem. The missing proof is.
>
> Most property apps digitise the landlord's notebook. They still assume one
> full monthly payment and one person holding the truth. We built the
> reconciliation layer instead.

Point at the audience, not the screen. Pause after "the missing proof is".

**The four copies:** tenant → receipt; caretaker → day close; owner →
statement; agent → ledger. One ledger produces all four.

## Part 2 — Today and the collection (Kabutha, 1:20–2:00)

Open the app on **Today**.

**Say:**

> This is the month written as a statement: Ksh 73,000 collected of
> Ksh 113,000 expected, and Ksh 52,000 still out across five tenants. The
> **Needs you** list puts the tenants, the open repairs and the vacancy in
> one place. The receipts below are today's proof.

Point at the statement, then the **Needs you** rows. Tap **Collect rent**.

> The caretaker can collect in seconds: pick the tenant, enter the amount,
> choose cash or M-Pesa, and the receipt is issued.

Select **Grace Wanjiku**, show the balance **Ksh 4,000**, and leave the amount
for the next speaker. Do **not** record yet.

## Part 3 — The tenant ledger and the receipt (Kwendo, 2:00–2:50)

In the collect sheet, set **Ksh 2,500**, choose **Cash**, tap **Record & issue
receipt**.

**Say:**

> Grace's balance was 4,000. She paid 2,500. The receipt number is
> sequential, the balance after is printed, and the tenant keeps a copy.
> This is the piece of paper that settles the argument.

The receipt screen animates the **SAHIHI** stamp. Tap **Share receipt** and
show the Android share sheet (WhatsApp is the first choice). Do not actually
send it if time is short; the sheet is the proof.

> Open the tenant ledger. Every charge and every payment is a line, with the
> running balance. Part payments are normal. The oldest unpaid month drives
> the ageing.

Go back to Today.

## Part 4 — M-Pesa reconciliation (Erika, 2:50–3:40)

Go to **Money → M-Pesa**. Paste a confirmation message:

> `QGH7X8Y9Z Confirmed. Ksh2,500.00 received from JOHN KAMAU 0745678901 on
> 24/11/26 at 10:42 AM`

Tap **Read message**.

**Say:**

> The app reads the amount, the phone, the date and the reference. It
> normalises the phone and matches John Kamau in Kasarani A4. It shows the
> balance before anything is saved. Nothing is written until the agent
> confirms.

Tap **Record M-Pesa & issue receipt**. The new receipt shows **Ksh 2,500**
and the new balance **Ksh 1,500**.

> We deliberately do not read SMS. Android restricts it, users should not
> grant it, and the paste is enough. The parser is pure Kotlin and tested.

## Part 5 — The owner statement (Erika, 3:40–4:30)

Go to **Buildings → Kasarani Bedsitters**. Scroll to the net-to-owner figure.

**Say:**

> This is the document that keeps the client. Collected Ksh 19,500. Expenses
> Ksh 4,000: water 2,500 and security 1,500. The management fee is 10%, so
> Ksh 1,950. Net to the owner: Ksh 13,550. Arrears and vacancy loss are shown
> separately so nothing is hidden inside the net.

Tap **Send owner statement**. Show the statement screen and the share
button.

## Part 6 — Repairs and the visit planner (Leon, 4:30–5:10)

From the building screen tap **Repairs**.

**Say:**

> Every repair has a priority, a status and a cost. A completed repair with a
> cost becomes an expense, so it reaches the owner statement automatically.

Open the roof leak. Show the **Urgent** priority and the cost field. Go back
to the building and scroll to **Visit planner**.

> The weather is not a decoration. Kasarani has 94% rain in the forecast and
> two open roof repairs. The agent plans the visit before the rain, not after.

## Part 7 — The daily brief (Naaadir, 5:10–5:45)

Go to **More → Send today's brief now**.

Pull down the notification shade.

**Say:**

> WorkManager builds the same ledger snapshot at 7am, even if the app is
> closed. Ksh 79,500 of Ksh 113,000 collected, four tenants owe Ksh 45,500,
> three repairs open. It survives the phone being switched off. That is why
> it is WorkManager and not a timer.

## Part 8 — Architecture and the technical story (Arnold, 5:45–6:45)

**Say:**

> The screen only shows. The ViewModel holds the state. The repository is the
> only place that knows whether the data came from Room or the API. Room keeps
> the ledger on the phone.

Draw the flow on the slide or point:

```
Fragment → ViewModel → Repository → Room
                              ↘ Open-Meteo
```

Three technical decisions worth defending:

1. **The ledger is derived, not stored.** Rent charges come from the tenancy
   dates and rent; payments are stored; the balance is calculated. There is no
   second copy of the truth to disagree.
2. **The database migrates.** Version 2 adds buildings, expenses and receipt
   fields with `Migration(1, 2)`. A version 1 phone keeps its records.
3. **The receipt is a real artefact.** Canvas draws it; FileProvider shares a
   scoped URI; WhatsApp receives an image.

## Part 9 — GitHub and the team (everyone, 6:45–8:15)

Open the GitHub repository:

`https://github.com/ArnoldCurtisStrath/property-management-system`

Show:

- the contributors graph (six members)
- the pull requests and reviews
- the v2 commits for Sahihi

Each member says one line:

- **Arnold:** the ledger engine, the Room migration, the architecture and the
  documentation.
- **Kabutha:** the design system, Today and the building screens.
- **Kwendo:** the tenant ledger, the receipt screen and the tenant form.
- **Erika:** the collect sheet, the M-Pesa parser, Money and the owner
  statement.
- **Leon:** the repairs, the weather card and the form/empty/error states.
- **Naaadir:** the forecast, the daily brief and the signed release.

## Part 10 — Close (Arnold, 8:15–9:15)

**Say:**

> Sahihi does not just record rent. It settles it.
>
> The tenant has a receipt. The caretaker has a clean day close. The owner has
> a statement. The agent has a business they can prove.
>
> Every shilling, accounted for.
>
> The next step is sync, so a caretaker's phone and an owner's phone can see
> the same ledger. The core is already here.

## If something goes wrong

- **Emulator is slow:** keep talking through the screen you have; the numbers
  are already on it. Do not wait in silence.
- **M-Pesa paste fails:** type the message again from the script, or use the
  already-recorded receipt in Money.
- **Share sheet does not open:** say "the receipt is generated locally; the
  share sheet is the last step" and move on.
- **Notification does not appear:** open More and tap the button again; the
  worker is one-time and immediate. If it still fails, show the notification
  from the earlier test in the screen recording.
- **A screen is missing:** use the bottom bar or the back arrow; do not open
  Settings mid-demo.

## Ten-second answers

- **Why this architecture?** The screen can change without touching the
  database, and the ledger can be tested without Android.
- **Why Room?** Real SQLite, offline, observable, and the records survive the
  app closing.
- **How does CRUD work?** Insert, query, update and delete are DAO functions;
  the repository calls them; the ViewModel observes them.
- **What about bad input?** `Validation` returns false; the field shows the
  message; nothing is saved.
- **What if the API is down?** The weather card shows a message and a retry;
  the ledger keeps working.
- **Why WorkManager?** The brief must run when the app is closed and after a
  reboot.
- **What would you improve?** Sync, photo evidence, lease reminders, recurring
  expenses, a tenant portal.
