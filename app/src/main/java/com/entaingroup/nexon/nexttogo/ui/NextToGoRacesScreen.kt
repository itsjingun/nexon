package com.entaingroup.nexon.nexttogo.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.entaingroup.nexon.nexttogo.NextToGoRacesContract
import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import kotlinx.coroutines.flow.Flow

@Composable
internal fun NextToGoRacesScreen(
    viewState: NextToGoRacesContract.ViewState,
    onCategoryChipClick: (RacingCategory) -> Unit,
    ticker: Flow<Unit>,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        var races by remember { mutableStateOf<List<Race>>(emptyList()) }

        val lazyListState = rememberLazyListState()

        when {
            viewState.isLoading -> {
                FullScreenLoadingIndicator()
            }

            viewState.racesFlow != null -> {
                LaunchedEffect(viewState.racesFlow) {
                    viewState.racesFlow.collect {
                        races = it
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    state = lazyListState,
                ) {
                    item {
                        Row {
                            RacingCategory.entries.forEach { category ->
                                FilterChip(
                                    onClick = { onCategoryChipClick(category) },
                                    label = {
                                        Text(category.name)
                                    },
                                    selected = viewState.selectedCategories.contains(category),
                                )
                            }
                        }
                    }

                    races.forEach { race ->
                        item(key = race.id) {
                            RaceCard(
                                race = race,
                                ticker = ticker,
                            )
                        }
                    }
                }
            }
        }
    }
}
