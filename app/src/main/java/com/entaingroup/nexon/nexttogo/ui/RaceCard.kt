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
import kotlin.math.abs

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

/**
 * Get time remaining until an [Instant] in the format:
 * - 5h 9m 4s
 * - 24m 48s
 * - 0s
 */
internal fun getTimeRemainingUntil(then: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(now, then)
    val isNegative = duration.seconds < 0
    val diff = abs(duration.seconds)

    val hours = diff / 3600
    val minutes = (diff % 3600) / 60
    val seconds = diff % 60

    val parts = mutableListOf<String>()
    if (hours > 0) parts.add("${hours}h")
    if (minutes > 0) parts.add("${minutes}m")
    if (seconds > 0 || (hours == 0L && minutes == 0L)) parts.add("${seconds}s")

    val result = parts.joinToString(" ")
    return if (isNegative) "-$result" else result
}
