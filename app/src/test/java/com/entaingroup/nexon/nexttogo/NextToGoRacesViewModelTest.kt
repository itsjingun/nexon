package com.entaingroup.nexon.nexttogo

import app.cash.turbine.test
import com.entaingroup.nexon.nexttogo.domain.NextToGoRacesAutoUpdater
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant

class NextToGoRacesViewModelTest {
    private val nextToGoRacesAutoUpdater: NextToGoRacesAutoUpdater = mockk()
    private val timeProvider: TimeProvider = mockk()

    private lateinit var viewModel: NextToGoRacesViewModel

    @Before
    fun setUp() {
        every { nextToGoRacesAutoUpdater.nextRaces } returns emptyFlow()
        every { nextToGoRacesAutoUpdater.backgroundErrors } returns emptyFlow()
        every { nextToGoRacesAutoUpdater.startRaceUpdates(any(), any()) } just Runs
        every { nextToGoRacesAutoUpdater.stopRaceUpdates() } just Runs
        every { timeProvider.now() } returns Instant.EPOCH

        viewModel = NextToGoRacesViewModel(
            nextToGoRacesAutoUpdater = nextToGoRacesAutoUpdater,
            timeProvider = timeProvider,
        )
    }

    @Test
    fun givenNextRaces_whenRaceUpdatesStarted_thenEmitRaces() = runTest {
        // Given

        val dummyRace1 = Race(
            id = "1",
            meetingName = "Foo",
            raceNumber = 12,
            category = RacingCategory.HORSE,
            startTime = Instant.EPOCH,
        )
        val dummyRace2 = Race(
            id = "1",
            meetingName = "Foo",
            raceNumber = 12,
            category = RacingCategory.HORSE,
            startTime = Instant.EPOCH,
        )
        every { nextToGoRacesAutoUpdater.nextRaces } returns flowOf(
            listOf(dummyRace1),
            listOf(dummyRace1, dummyRace2),
        )

        viewModel.viewState.test {
            // When

            viewModel.initialize()

            assertEquals(emptyList<Race>(), awaitItem().races)
            verify(exactly = 1) { nextToGoRacesAutoUpdater.startRaceUpdates(any(), any()) }

            // Then

            assertEquals(listOf(dummyRace1), awaitItem().races)
            assertEquals(listOf(dummyRace1, dummyRace2), awaitItem().races)
        }
    }

    @Test
    fun givenAllCategoriesSelected_whenCategoryToggled_thenRemoveCategory() = runTest {
        val initialCategories = setOf(
            RacingCategory.GREYHOUND,
            RacingCategory.HARNESS,
            RacingCategory.HORSE,
        )
        val updatedCategories = setOf(
            RacingCategory.HARNESS,
            RacingCategory.HORSE,
        )

        viewModel.viewState.test {
            // Given

            viewModel.initialize()

            assertEquals(initialCategories, awaitItem().selectedCategories)
            verify(exactly = 1) {
                nextToGoRacesAutoUpdater.startRaceUpdates(
                    any(),
                    initialCategories,
                )
            }

            // When

            viewModel.toggleRacingCategory(RacingCategory.GREYHOUND)

            // Then

            assertEquals(updatedCategories, awaitItem().selectedCategories)
            verify(exactly = 1) {
                nextToGoRacesAutoUpdater.startRaceUpdates(
                    any(),
                    updatedCategories,
                )
            }
        }
    }

    @Test
    fun givenNoCategoriesSelected_whenCategoryToggled_thenAddCategory() = runTest {
        viewModel.viewState.test {
            // Given

            viewModel.initialize()
            viewModel.toggleRacingCategory(RacingCategory.GREYHOUND)
            awaitItem()
            viewModel.toggleRacingCategory(RacingCategory.HARNESS)
            awaitItem()
            viewModel.toggleRacingCategory(RacingCategory.HORSE)
            awaitItem()

            assertEquals(emptySet<RacingCategory>(), awaitItem().selectedCategories)

            // When

            viewModel.toggleRacingCategory(RacingCategory.HARNESS)

            // Then

            assertEquals(setOf(RacingCategory.HARNESS), awaitItem().selectedCategories)
            verify(exactly = 1) {
                nextToGoRacesAutoUpdater.startRaceUpdates(
                    any(),
                    setOf(RacingCategory.HARNESS),
                )
            }
        }
    }

    @Test
    fun givenNoInternet_whenRaceUpdatesStarted_thenShowError() = runTest {
        // Given

        every { nextToGoRacesAutoUpdater.backgroundErrors } returns flowOf(IOException())

        viewModel.viewState.test {
            // When

            viewModel.initialize()

            assertEquals(false, awaitItem().showError)
            verify { nextToGoRacesAutoUpdater.startRaceUpdates(any(), any()) }

            // Then

            assertEquals(true, awaitItem().showError)
            verify { nextToGoRacesAutoUpdater.stopRaceUpdates() }
        }
    }
}
