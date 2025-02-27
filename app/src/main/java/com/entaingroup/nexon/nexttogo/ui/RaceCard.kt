package com.entaingroup.nexon.nexttogo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.nexttogo.domain.Race
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.math.max

@Composable
internal fun RaceCard(
    race: Race,
    ticker: Flow<Unit>,
) {
    var timeRemaining by remember {
        mutableStateOf(getTimeRemainingUntil(race.startTime))
    }

    LaunchedEffect(Unit) {
        ticker.collect {
            timeRemaining = getTimeRemainingUntil(race.startTime)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = race.name)
            Text(text = timeRemaining)
        }
    }
}

private fun getTimeRemainingUntil(then: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(now, then)
    val diff = max(duration.seconds, 0)

    val hours = diff / 3600
    val minutes = (diff % 3600) / 60
    val seconds = diff % 60

    val format: String
    val args: Array<Long>
    if (hours > 0) {
        format = "%01d:%02d:%02d"
        args = arrayOf(hours, minutes, seconds)
    } else {
        format = "%01d:%02d"
        args = arrayOf(minutes, seconds)
    }

    return String.format(Locale.getDefault(), format, *args)
}
