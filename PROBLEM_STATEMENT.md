# The problem statement

## Theme

Real Estate and Property Management.

## 1. What problem exists?

Small landlords keep track of their properties, their tenants, the rent that
was paid and the repairs that are needed on paper or in a spreadsheet. When
the number of houses grows, the records stop agreeing with each other and
important things are missed.

## 2. Who experiences the problem?

Landlords and letting agents who rent out a small number of properties, most
of them in Kenya, and who do not have a property management system.

## 3. Why is the problem important?

Rent is how most of these landlords earn. When a payment is forgotten or
recorded wrongly the landlord loses money, and when a repair is not tracked
the tenant loses trust. Both end the rental relationship.

## 4. How is it currently handled?

With a notebook, a file of papers and a phone calculator. Some use a
spreadsheet, but it is only on one device, it is not easy to update from
where the landlord is standing, and there is no way to see at a glance which
tenant has not paid.

## 5. What are the limitations of the current approach?

- The records are only as good as the person keeping them, and they are easy
  to lose.
- There is no reminder, so an unpaid rent is only noticed when the landlord
  asks for it.
- A landlord who is not at home cannot look anything up.
- There is no link between a repair and the house it belongs to, so problems
  get fixed twice or forgotten.

## 6. How does the app solve it?

The app keeps the properties, the tenants, the payments and the maintenance
requests in one place on the phone.

- A property is added once and every tenant, payment and request is linked
  to it.
- A payment is recorded against a tenant and the month it was paid for, so
  the app can show what was collected and what is still outstanding.
- A maintenance request is raised against a property and moves through Open,
  In progress and Done.
- A background check runs every day and reminds the landlord about the
  tenants who have not paid for the month.
- The weather at the property is fetched from the Open-Meteo API, which
  helps the landlord plan a visit.

The app works without a network, so it can be used anywhere. The records
stay on the phone in a Room database and are still there after the app is
closed.
