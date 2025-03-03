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
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.R
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesContract
import kotlinx.coroutines.flow.Flow

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
