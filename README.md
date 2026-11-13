# Property Management System

An Android application that helps a landlord keep track of the properties they
rent out. The properties, the tenants living in them, the rent that has been
paid and the repairs that still need to be done are all kept in one place on
the phone.

Built with Kotlin for the BBIT 3.2 Mobile Application Development CAT 2.

## The problem

Small landlords keep track of their tenants, their rent and the repairs that
are needed on paper, in a notebook or in a spreadsheet. The records get lost,
the rent that was paid is forgotten and there is no way to tell which
tenant still owes money or which house has a problem that has not been fixed.

This app keeps all of it in one place. It works offline, so it can be used
anywhere, even in a place with no network.

## What the app does

| Part | What it does |
|---|---|
| Properties | Add, change and remove the houses that are rented out, with the type, the rent and the state of each one |
| Tenants | Add the people who live in each property, with their phone number, their email and the rent they agreed to pay |
| Payments | Record the rent that has been paid and see the money that was collected and the money that is still outstanding |
| Maintenance | Raise a problem that has to be fixed and move it from Open to In progress to Done |
| Weather | Shows the current weather at the address of a property, using the Open-Meteo API |
| Rent reminder | A background check that reminds the landlord about tenants who have not paid for the month |

## Screens

There are fourteen screens:

1. Sign in
2. Create an account
3. Dashboard
4. Property list
5. Add or change a property
6. Property details
7. Tenant list
8. Add or change a tenant
9. Tenant details
10. Payment history
11. Record a payment
12. Maintenance list
13. Raise or change a request
14. About

## How the app is built

The app follows the architecture that separates the screen from the data:

```
User  ->  Fragment (the screen)  ->  ViewModel  ->  Repository  ->  Room database
                                                          ->  Open-Meteo API
```

- The **fragment** only shows things and passes on what the user typed.
- The **ViewModel** holds the state of the screen, so the data survives when
  the phone is turned and the screen is built again.
- The **Repository** is the only place that knows where the data comes from.
- The **Room database** keeps the records on the phone.

The background work (the rent reminder) sits outside the screens and uses
WorkManager.

### Where the code is

```
app/src/main/java/com/example/propertymanagementsystem/
  data/model         the four tables as Kotlin classes
  data/local         the DAOs and the Room database
  data/remote        the class that talks to the weather API
  data/repository    one repository for each table
  ui/auth            the sign in and create account screens
  ui/main            the main screen with the drawer
  ui/dashboard       the summary of the portfolio
  ui/properties      the property screens
  ui/tenants         the tenant screens
  ui/payments        the payment screens
  ui/maintenance     the maintenance screens
  ui/about           the about screen
  ui/viewmodel       the ViewModels
  work               the background worker and the notification
  util               the checks used on the forms and the date helpers
```

## The tools used

- Kotlin and XML layouts with ViewBinding
- Room for the database
- Lifecycle ViewModel and LiveData for the screen state
- Navigation component for the screens
- RecyclerView for the lists
- WorkManager for the daily rent check
- Coroutines for the work that must not block the screen
- HttpURLConnection and the JSON reader that come with Android for the API

## How to run it

1. Open the project in Android Studio.
2. Let Gradle finish the first build.
3. Run the app on a phone or on an emulator.

The app needs the internet only for the weather. Everything else works
without it.

## The team

| Name | Admission number | Part | Pull requests |
|---|---|---|---|
| Arnold Wangai Mbici | 166569 | Lead, the data layer and the documentation | [#1](https://github.com/ArnoldCurtisStrath/property-management-system/pull/1), [#2](https://github.com/ArnoldCurtisStrath/property-management-system/pull/2), [#10](https://github.com/ArnoldCurtisStrath/property-management-system/pull/10) |
| Kabutha Joe Mwangi | 172896 | The properties | [#3](https://github.com/ArnoldCurtisStrath/property-management-system/pull/3) |
| Kwendo Andrew Abel | 192106 | The tenants | [#4](https://github.com/ArnoldCurtisStrath/property-management-system/pull/4) |
| Lusiola Erika Wambui | 177886 | The payments | [#5](https://github.com/ArnoldCurtisStrath/property-management-system/pull/5) |
| Leon Onyancha Ouya | 189367 | The maintenance requests | [#7](https://github.com/ArnoldCurtisStrath/property-management-system/pull/7), [#8](https://github.com/ArnoldCurtisStrath/property-management-system/pull/8) |
| Naaadir Abdulaziz Rana | 189548 | The API, the background work and the release | [#6](https://github.com/ArnoldCurtisStrath/property-management-system/pull/6), [#9](https://github.com/ArnoldCurtisStrath/property-management-system/pull/9), [#11](https://github.com/ArnoldCurtisStrath/property-management-system/pull/11) |

Every part was built on its own branch and was reviewed by another member
before it was merged.

## The release

Version 1.0 is on the releases page of the repository. The APK is signed and
can be installed on any phone that runs Android 7 or later.
