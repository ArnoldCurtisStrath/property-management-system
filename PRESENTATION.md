# The presentation

The presentation is split into the five parts the brief asks for. The
members are named so everybody knows who is speaking.

## Part 1 - The problem (Arnold)

- Small landlords keep their records on paper and forget them.
- Rent that was not paid is only noticed when the landlord asks for it.
- Repairs are not linked to the house they belong to.

The full statement is in PROBLEM_STATEMENT.md.

## Part 2 - The solution (Arnold)

The Property Management System app. Show the four parts:

- Properties and the tenants living in them
- The rent that was collected and what is outstanding
- The maintenance requests and where they have reached
- The daily reminder and the weather at a property

## Part 3 - The architecture (Kabutha)

```
User -> Fragment -> ViewModel -> Repository -> Room database
                                         -> Open-Meteo API
```

- The fragment shows the screen and nothing else.
- The ViewModel holds the state so the screen survives being turned.
- The repository is the only place that knows where the data is from.
- Room keeps the records on the phone.

## Part 4 - The demonstration (Erika and Leon)

1. Sign in and show the dashboard.
2. Add a property (Kabutha).
3. Add a tenant in that property (Kwendo).
4. Record a rent payment and show what is outstanding (Erika).
5. Raise a maintenance request and move it to Done (Leon).
6. Open a property and show the weather from the API (Naaadir).
7. Show the rent reminder notification (Naaadir).

## Part 5 - The technical defence (everyone)

Each member must be ready to explain their own part:

| Member | Has to be able to explain |
|---|---|
| Arnold | The Room database, the DAOs, the repositories, the README and the problem statement |
| Kabutha | The property list, the RecyclerView and the adapter, the validation on the property form, the duplicate check |
| Kwendo | The tenant list, the link between a tenant and a property, the validation of the phone number and the email, the search |
| Erika | Recording a payment, the totals for the month, how the outstanding amount is worked out |
| Leon | The maintenance list, the states of a request, the delete confirmation, the empty and error states |
| Naaadir | The API call, the JSON that comes back, the error handling and the retry, the worker, why WorkManager is used |

Questions that are likely to be asked:

- Why this architecture? It keeps the screen apart from the data, so the
  screen can be changed without touching the database.
- Why Room? It is a real SQLite database with the queries written as
  Kotlin functions, and the data is still there after the app is closed.
- How does the CRUD work? Insert, query, update and delete are in the DAOs
  and the repository calls them.
- What happens with wrong input? The check in Validation returns false and
  the form shows a message under the field.
- How does the data move? Fragment to ViewModel, ViewModel to repository,
  repository to the DAO, DAO to the database.
- What if the API is down? The API returns null, the card shows a message and
  there is a button to try again.
- Why WorkManager? The check has to run when the app is not open and it
  should survive the phone being switched off. WorkManager keeps the work
  and does it later.
- What would you improve? Sync between phones, photos of the properties and
  a monthly report.
