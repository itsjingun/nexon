<img src="https://i.imgur.com/pJ7iOmA.png" width=240 height=240 />

# Nexon

Nexon is an Android application that retrieves and displays upcoming horse, harness, and greyhound races, simulating the "Next to Go" feature commonly found on sports betting platforms. The app dynamically updates the race list in real time, ensuring users always see the most relevant upcoming events.

Dark theme is also supported.

## Table of Contents

- [Key features](#key-features)
- [Architecture overview](#architecture-overview)
- [How the app updates data automatically](#how-the-app-updates-data-automatically)
  - [Technical breakdown](#technical-breakdown)
  - [Assumptions](#assumptions)
- [Improvements for the future](#improvements-for-the-future)
- [Tools and libraries used](#tools-and-libraries-used)
- [Running the app](#running-the-app)

## Key features

| Smart Loading | Automatic Removal | Filter by Category |
|-|-|-|
| <img src="https://github.com/user-attachments/assets/cb077bdb-3ca6-4b3a-9db2-611f52edfeed" width=240 /> | <img src="https://github.com/user-attachments/assets/7f8ff9a8-beb5-468f-8c65-335c440b1150" width=240 /> | <img src="https://github.com/user-attachments/assets/ffc83696-05fb-4dae-8285-db0a52ad70e9" width=240 /> |

#### Dark Theme

<img src="https://github.com/user-attachments/assets/99756ce3-2cb3-4eb7-940b-48734881a5aa" width=340>

## Architecture overview

This project follows a basic **Domain-Drivenesque Architecture** structured into three main layers:
- **Data** Layer `data/` - Contains data sources (Local database, API etc).
- **Domain** Layer `domain/` - Contains business logic and domain models.
- **UI** Layer `ui/` - Contains all presentation logic (ViewModels, Screens, Composables etc).

 Here is an overview of the main package structure:
```
com.entaingroup.nexon
├── ...
├── nexttogo                # Main package for "Next to go" domain
│   ├── data                # Data layer
│   │   ├── api             # For network API requests
│   │   ├── mapping         # Mapping logic between data objects
│   │   ├── persisted       # For database (Room)
│   ├── di                  # Dependencies (Hilt modules)
│   ├── domain              # Domain layer
│   │   ├── model           # Domain models (e.g. Race)
│   └── ui                  # UI layer
│       └── composable      # Composables
└── ...
```

There are two unit test suites (which hopefully should cover the main logical flows):
- `NextToGoRacesViewModelTest` for the ViewModel
- `DefaultNextToGoRacesInteractorTest` for the bulk of the business logic

## How the app updates data automatically

How the live race updates for the "Next to Go" feature essentially works:
- The race data for the UI comes from a local Room database.
- Races are fetched from an API only when needed, and stored in the database.
- The UI is updated whenever the database is updated (via a `Flow`).
- The app keeps track of when a race "expires" (1 minute after starting) and reacts accordingly to keep the UI up to date.

### Technical breakdown

*Please see [DefaultNextToGoRacesInteractor.kt](app/src/main/java/com/entaingroup/nexon/nexttogo/data/DefaultNextToGoRacesInteractor.kt).*

When the main Activity is launched, `NextToGoRacesInteractor.startRaceUpdates()` will be called to start the automatic race updates:
```
internal interface NextToGoRacesInteractor {
    /**  
     * A [Flow] that emits a list of upcoming [Race]s in chronological order.
     */
    val nextRaces: Flow<List<Race>>  
  
    /**  
     * Starts the automatic updates (for retrieving data for [nextRaces]).
     */
    fun startRaceUpdates(count: Int, categories: Set<RacingCategory>)
}
```
and collecting from `nextRaces` will start the following logic:

1. Stored Race data will be retrieved from a Room database (backed by a DAO `Flow`):
```
@Dao  
internal interface DbRaceDao {  
    @Query(  
        """  
        SELECT * FROM race
        WHERE start_time >= :minStartTime
        ORDER BY start_time, meeting_name
        LIMIT :count
        """,  
    )  
    fun getNextRaces(count: Int, minStartTime: Long): Flow<List<DbRace>>
    ...
}
```
2. The interactor checks if a sufficient number of races was provided (keep in mind the app is always trying to show 5 races on the screen):
	- If there is enough data, then nothing will be done until the time when the earliest race expires.
	- If there is not enough data, more data is fetched via the `rest/v1/racing/` API endpoint, and subsequently stored in the database (stale data is also deleted at the same time).
3. If the database was updated in step 2, the database query is automatically rerun and new data is emitted to the UI.
4. If there are still not enough races, data is fetched again from the API but with a higher `count` (to simulate pagination). The `count` will keep increasing after each subsequent API call (i.e. 10, 20, 30, 40 and so on) until there is enough data to sufficiently fill 5 races in the UI. There is also an added delay between each API call to prevent too many network requests being made. *Note: This crude way of pagination was employed because of the limitations of the API endpoint (which I'm assuming is part of this technical challenge).*

Meanwhile, an internal ticker runs every second to check whether the current time has reached the earliest race's expiry time. When that is reached, the above logical flow will resume from step 1 (if it isn't running already), triggering a database query to remove any expired races, and potentially fetch more data.

### Assumptions

- `GET rest/v1/racing/` will never return a duplicate entry, or different items with the same ID.
- `GET rest/v1/racing/` will never return races that have already "expired".
- Resources from `GET rest/v1/racing/` will always return a non-null `race_summaries, race_id`, `race_number`, `meeting_name`, `category_id`, and `advertised_start`.

## Improvements for the future

- **Improved error handling.** Currently, if any exception is encountered during the background operation of `DefaultNextToGoRacesInteractor`, the UI will stop the updates and display a full screen error state (I kept it simple for time's sake). Naturally, we could implement some improved error handling where the app responds differently depending on the type of error. It could also show partial error states instead of replacing all the information on the screen.
- **Smarter fetch counts.** At the moment, whenever you change category filters and there aren't enough races, the fetch count resets back to 10 (even if you've been on that filter combination before). It might make sense to keep track of fetches for filter combinations so that the next fetch has a larger count.
- **Manual refresh.** Currently, all data fetching is handled automatically and the user has no way of explicitly refreshing the list of races.
- **More unit tests.** I only added tests for the most common flows, because it would take too much time to add tests for every possible flow and edge case. If this were ever to be used in production, more coverage would naturally be ideal. Instrumented tests could also be added to test DAO operations using Room's in-memory database mode.

## Tools and libraries used

Some of the tools used:
- **UI**
	- Compose / Material3
- **State Management and Threading**
	- Flows
	- Coroutines
- **Data**
	- Room
- **Networking**
	- Retrofit
- **Dependency Injection**
	- Hilt
- **Logging**
	- Timber
- **Testing**
	- Turbine
	- Mockk
- **Code Quality**
	- ktlint

## Running the app

This project can be opened in Android Studio and run like any standard Android application. Simply open the project, ensure dependencies are installed, select a device or emulator, and run!
