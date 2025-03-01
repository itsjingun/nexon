package com.entaingroup.nexon.nexttogo.ui

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.R
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

        LaunchedEffect(viewState.racesFlow) {
            viewState.racesFlow.collect { races = it }
        }

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
                    races.getOrNull(i)?.let { race ->
                        item(key = race.id, contentType = "race") {
                            RaceCard(
                                race = race,
                                ticker = ticker,
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
