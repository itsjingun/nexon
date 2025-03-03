package com.entaingroup.nexon.nexttogo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entaingroup.nexon.nexttogo.ui.composable.NextToGoRacesScreen
import com.entaingroup.nexon.ui.theme.NexonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NextToGoRacesActivity : ComponentActivity() {
    private val viewModel: NextToGoRacesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val viewState by viewModel.viewState.collectAsStateWithLifecycle()

            NexonTheme {
                NextToGoRacesScreen(
                    viewState = viewState,
                    timeProvider = viewModel.timeProvider,
                    onCategoryChipClick = viewModel::toggleRacingCategory,
                    onTryAgainButtonClick = viewModel::onTryAgainButtonClick,
                    ticker = viewModel.ticker,
                )
            }
        }

        viewModel.initialize()
    }
}
