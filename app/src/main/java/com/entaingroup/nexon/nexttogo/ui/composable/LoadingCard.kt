package com.entaingroup.nexon.nexttogo.ui.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.ui.theme.NexonTheme
import kotlinx.coroutines.flow.emptyFlow
import java.time.Instant

private val DummyRace = Race(
    id = "",
    meetingName = "",
    raceNumber = 0,
    category = RacingCategory.UNKNOWN,
    startTime = Instant.EPOCH,
)

private val DummyTimeProvider = object : TimeProvider {
    override fun now() = Instant.EPOCH
}

@Composable
internal fun LoadingCard() {
    // Animate the background alpha to create simple shimmering effect.
    val alpha by rememberInfiniteTransition(
        label = "InfiniteTransition",
    ).animateFloat(
        initialValue = 0.6f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Shimmer",
    )

    Box(
        modifier = Modifier
            .clip(CardDefaults.shape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
    ) {
        // Use an invisible Race card to provide approximate sizing.
        RaceCard(
            race = DummyRace,
            timeProvider = DummyTimeProvider,
            ticker = emptyFlow(),
            modifier = Modifier.alpha(0f),
        )
    }
}

// region Previews

@Preview
@Composable
internal fun Preview_LoadingCard() {
    NexonTheme {
        LoadingCard()
    }
}

// endregion
