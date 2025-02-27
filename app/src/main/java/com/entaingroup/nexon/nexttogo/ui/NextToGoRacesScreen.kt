package com.entaingroup.nexon.nexttogo.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.entaingroup.nexon.nexttogo.NextToGoRacesContract
import kotlinx.coroutines.flow.Flow

@Composable
internal fun NextToGoRacesScreen(
    viewState: NextToGoRacesContract.ViewState,
    ticker: Flow<Unit>,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        val lazyListState = rememberLazyListState()

        when {
            viewState.isLoading -> {
                FullScreenLoadingIndicator()
            }

            viewState.races != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    state = lazyListState,
                ) {
                    viewState.races.forEach { race ->
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
