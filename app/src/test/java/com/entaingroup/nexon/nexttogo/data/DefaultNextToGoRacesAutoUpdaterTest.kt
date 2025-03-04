package com.entaingroup.nexon.nexttogo.data

import com.entaingroup.nexon.dispatcher.DispatcherProvider
import com.entaingroup.nexon.nexttogo.data.api.NextToGoRacesApi
import com.entaingroup.nexon.nexttogo.data.mapping.toDbRaces
import com.entaingroup.nexon.nexttogo.data.persisted.DbRaceDao
import com.entaingroup.nexon.nexttogo.data.persisted.FakeDbRaceDao
import com.entaingroup.nexon.nexttogo.data.persisted.NextToGoDatabase
import com.entaingroup.nexon.nexttogo.data.persisted.toRaces
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNextToGoRacesAutoUpdaterTest {
    private lateinit var testDispatcher: TestDispatcher

    private lateinit var autoUpdater: DefaultNextToGoRacesAutoUpdater
    private val nextToGoRacesApi: NextToGoRacesApi = mockk()
    private val nextToGoDatabase: NextToGoDatabase = mockk()
    private lateinit var dbRaceDao: DbRaceDao

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()

        dbRaceDao = FakeDbRaceDao()
        every { nextToGoDatabase.dbRaceDao() } returns dbRaceDao

        autoUpdater = DefaultNextToGoRacesAutoUpdater(
            nextToGoRacesApi,
            nextToGoDatabase,
            object : TimeProvider {
                override fun now(): Instant {
                    return Instant.ofEpochMilli(testDispatcher.scheduler.currentTime)
                }
            },
            object : DispatcherProvider {
                override fun main() = testDispatcher
            },
        )
    }

    @Test
    fun givenSufficientRaces_whenNoRaceHasExpired_thenFetchOnlyOnce() = runTest(testDispatcher) {
        // Given

        val firstApiResponse = sufficientNumberOfRaces()
        coEvery { nextToGoRacesApi.getNextRaces(any(), any()) } returns firstApiResponse

        // When

        // Capture race emissions.
        val values = mutableListOf<List<Race>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            autoUpdater.nextRaces.toList(values)
        }

        autoUpdater.startRaceUpdates(5, emptySet())
        runCurrent()
        advanceTimeBy(59000)
        autoUpdater.stopRaceUpdates()
        runCurrent()

        // Then

        // Verify emissions.
        assertEquals(2, values.size)
        assertEquals(emptyList<Race>(), values[0])
        assertEquals(
            firstApiResponse.toDbRaces().sortedBy { it.startTime }.take(5).toRaces(),
            values[1],
        )

        // Verify fetch calls.
        val fetchCounts = mutableListOf<Int>()
        coVerify(exactly = 1) {
            nextToGoRacesApi.getNextRaces("nextraces", capture(fetchCounts))
        }
        assertEquals(10, fetchCounts[0])
    }

    @Test
    fun givenSufficientRaces_whenFirstRaceHasExpired_thenFetchAgain() = runTest(testDispatcher) {
        // Given

        val firstApiResponse = sufficientNumberOfRaces()
        coEvery { nextToGoRacesApi.getNextRaces(any(), any()) } returns firstApiResponse

        // When

        // Capture race emissions.
        val values = mutableListOf<List<Race>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            autoUpdater.nextRaces.toList(values)
        }

        autoUpdater.startRaceUpdates(5, emptySet())
        runCurrent()
        advanceTimeBy(60001)
        autoUpdater.stopRaceUpdates()
        runCurrent()

        // Then

        // Verify emissions.
        assertEquals(3, values.size)
        assertEquals(emptyList<Race>(), values[0])
        assertEquals(
            firstApiResponse.toDbRaces().sortedBy { it.startTime }.take(5).toRaces(),
            values[1],
        )

        // Verify fetch calls.
        val fetchCounts = mutableListOf<Int>()
        coVerify(exactly = 2) {
            nextToGoRacesApi.getNextRaces("nextraces", capture(fetchCounts))
        }
        assertEquals(10, fetchCounts[0])
        assertEquals(10, fetchCounts[0])
    }

    @Test
    fun givenNotEnoughRaces_whenTickerHasElapsed_thenFetchAgain() = runTest(testDispatcher) {
        // Given

        val firstApiResponse = notEnoughRaces()
        val secondApiResponse = sufficientNumberOfRaces()
        coEvery { nextToGoRacesApi.getNextRaces(any(), any()) } returnsMany listOf(
            firstApiResponse,
            secondApiResponse,
        )

        // When

        // Capture race emissions.
        val values = mutableListOf<List<Race>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            autoUpdater.nextRaces.toList(values)
        }

        autoUpdater.startRaceUpdates(5, emptySet())
        runCurrent()
        advanceTimeBy(1001)
        autoUpdater.stopRaceUpdates()
        runCurrent()

        // Then

        // Verify emissions.
        assertEquals(3, values.size)
        assertEquals(emptyList<Race>(), values[0])
        assertEquals(
            firstApiResponse.toDbRaces().sortedBy { it.startTime }.take(5).toRaces(),
            values[1],
        )
        assertEquals(
            secondApiResponse.toDbRaces().sortedBy { it.startTime }.take(5).toRaces(),
            values[2],
        )

        // Verify fetch calls.
        val fetchCounts = mutableListOf<Int>()
        coVerify(exactly = 2) {
            nextToGoRacesApi.getNextRaces("nextraces", capture(fetchCounts))
        }
        assertEquals(10, fetchCounts[0])
        assertEquals(20, fetchCounts[1])
    }

    @Test
    fun givenSufficientRaces_whenCategoriesAreSelectedAndLessThanFiveRaces_thenFetchAgain() =
        runTest(testDispatcher) {
            // Given

            val selectedCategories = setOf(RacingCategory.GREYHOUND, RacingCategory.HARNESS)
            val firstApiResponse = sufficientNumberOfRaces()
            coEvery { nextToGoRacesApi.getNextRaces(any(), any()) } returns firstApiResponse

            // When

            // Capture race emissions.
            val values = mutableListOf<List<Race>>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                autoUpdater.nextRaces.toList(values)
            }

            autoUpdater.startRaceUpdates(5, selectedCategories)
            runCurrent()
            advanceTimeBy(1000)
            autoUpdater.stopRaceUpdates()
            runCurrent()

            // Then

            // Verify emissions.
            assertEquals(2, values.size)
            assertEquals(emptyList<Race>(), values[0])
            assertEquals(
                firstApiResponse
                    .toDbRaces()
                    .filter { selectedCategories.map { c -> c.id }.contains(it.categoryId) }
                    .sortedBy { it.startTime }
                    .take(5)
                    .toRaces(),
                values[1],
            )

            // Verify fetch calls.
            val fetchCounts = mutableListOf<Int>()
            coVerify(exactly = 2) {
                nextToGoRacesApi.getNextRaces("nextraces", capture(fetchCounts))
            }
            assertEquals(10, fetchCounts[0])
            assertEquals(20, fetchCounts[1])
        }

    @Test
    fun givenNoInternet_whenFetchFromApiFails_thenEmitError() = runTest(testDispatcher) {
        // Given

        val ioException = IOException()
        coEvery { nextToGoRacesApi.getNextRaces(any(), any()) } throws ioException

        // When

        // Capture emissions.
        val races = mutableListOf<List<Race>>()
        val errors = mutableListOf<Exception>()
        val backgroundDispatcher = UnconfinedTestDispatcher(testScheduler)
        backgroundScope.launch(backgroundDispatcher) {
            autoUpdater.nextRaces.toList(races)
        }
        backgroundScope.launch(backgroundDispatcher) {
            autoUpdater.backgroundErrors.toList(errors)
        }

        autoUpdater.startRaceUpdates(5, emptySet())
        runCurrent()
        autoUpdater.stopRaceUpdates()
        runCurrent()

        // Then

        // Verify emissions.
        assertEquals(1, races.size)
        assertEquals(1, errors.size)
        assertEquals(ioException, errors[0])

        // Verify fetch calls.
        val fetchCounts = mutableListOf<Int>()
        coVerify(exactly = 1) {
            nextToGoRacesApi.getNextRaces("nextraces", capture(fetchCounts))
        }
        assertEquals(10, fetchCounts[0])
    }
}
