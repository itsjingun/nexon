package com.entaingroup.nexon.nexttogo.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.R
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesContract
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesContract.Companion.DEFAULT_CATEGORIES
import com.entaingroup.nexon.ui.theme.NexonTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Instant

@Composable
internal fun NextToGoRacesScreen(
    viewState: NextToGoRacesContract.ViewState,
    timeProvider: TimeProvider,
    onCategoryChipClick: (RacingCategory) -> Unit,
    onTryAgainButtonClick: () -> Unit,
    ticker: Flow<Unit>,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = stringResource(R.string.next_to_go_heading),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp),
                style = MaterialTheme.typography.headlineLarge,
            )

            if (viewState.showError) {
                ErrorState(onTryAgainButtonClick = onTryAgainButtonClick)
            } else {
                CategoryFilterList(
                    categories = viewState.categories,
                    selectedCategories = viewState.selectedCategories,
                    onCategoryChipClick = onCategoryChipClick,
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    (0 until NextToGoRacesContract.MAX_NUMBER_OF_RACES).forEach { i ->
                        viewState.races.getOrNull(i)?.let { race ->
                            item(key = race.id, contentType = "race") {
                                RaceCard(
                                    race = race,
                                    timeProvider = timeProvider,
                                    ticker = ticker,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        } ?: item(contentType = "loading") {
                            LoadingCard()
                        }
                    }
                }
            }
        }
    }
}

// region Previews

@PreviewFontScale

@Preview
@Composable
internal fun Preview_NextToGoRacesScreen(
    @PreviewParameter(ViewStateProvider::class)
    viewState: NextToGoRacesContract.ViewState,
) {
    NexonTheme {
        NextToGoRacesScreen(
            viewState = viewState,
            timeProvider = object : TimeProvider {
                override fun now() = Instant.EPOCH
            },
            onCategoryChipClick = {},
            onTryAgainButtonClick = {},
            ticker = emptyFlow(),
        )
    }
}

internal class ViewStateProvider :
    PreviewParameterProvider<NextToGoRacesContract.ViewState> {
    override val values = sequenceOf(
        UNFILLED,
        PARTIALLY_FILLED,
        FILLED,
        ERROR,
    )

    companion object {
        val UNFILLED = NextToGoRacesContract.ViewState(
            races = emptyList(),
            categories = DEFAULT_CATEGORIES,
            selectedCategories = DEFAULT_CATEGORIES,
            showError = false,
        )
        val PARTIALLY_FILLED = NextToGoRacesContract.ViewState(
            races = (1..3).map { i ->
                Race(
                    id = "$i",
                    meetingName = "Greyhoundwick",
                    raceNumber = i,
                    category = RacingCategory.GREYHOUND,
                    startTime = Instant.ofEpochSecond(i * 123L),
                )
            },
            categories = DEFAULT_CATEGORIES,
            selectedCategories = DEFAULT_CATEGORIES,
            showError = false,
        )
        val FILLED = NextToGoRacesContract.ViewState(
            races = (1..5).map { i ->
                Race(
                    id = "$i",
                    meetingName = "Greyhoundwick",
                    raceNumber = i,
                    category = RacingCategory.GREYHOUND,
                    startTime = Instant.ofEpochSecond(i * 123L),
                )
            },
            categories = DEFAULT_CATEGORIES,
            selectedCategories = DEFAULT_CATEGORIES,
            showError = false,
        )
        val ERROR = NextToGoRacesContract.ViewState(
            races = emptyList(),
            categories = DEFAULT_CATEGORIES,
            selectedCategories = DEFAULT_CATEGORIES,
            showError = true,
        )
    }
}

// endregion
