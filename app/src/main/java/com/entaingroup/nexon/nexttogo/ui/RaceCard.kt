package com.entaingroup.nexon.nexttogo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.R
import com.entaingroup.nexon.nexttogo.domain.Race
import com.entaingroup.nexon.nexttogo.domain.RacingCategory
import com.entaingroup.nexon.ui.theme.NexonTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

@Composable
internal fun RaceCard(
    race: Race,
    ticker: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(category = race.category)
            Spacer(modifier = Modifier.width(16.dp))
            Title(meetingName = race.meetingName, raceNumber = race.raceNumber)
            Spacer(modifier = Modifier.width(16.dp))
            Countdown(startTime = race.startTime, ticker = ticker)
        }
    }
}

@Composable
private fun CategoryIcon(category: RacingCategory) {
    val drawableId: Int
    val contentDescriptionId: Int

    when (category) {
        RacingCategory.GREYHOUND -> {
            drawableId = R.drawable.ic_greyhound
            contentDescriptionId = R.string.racing_category_greyhound
        }

        RacingCategory.HARNESS -> {
            drawableId = R.drawable.ic_harness
            contentDescriptionId = R.string.racing_category_harness
        }

        RacingCategory.HORSE -> {
            drawableId = R.drawable.ic_horse
            contentDescriptionId = R.string.racing_category_horse
        }

        RacingCategory.UNKNOWN -> {
            drawableId = R.drawable.ic_launcher_foreground
            contentDescriptionId = R.string.racing_category_unknown
        }
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = stringResource(contentDescriptionId),
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RowScope.Title(
    meetingName: String,
    raceNumber: Int,
) {
    Text(
        text = buildAnnotatedString {
            append(meetingName.trim())
            append(" ")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Normal,
                )
            ) {
                append("R${raceNumber}")
            }
        },
        modifier = Modifier.weight(1.0f),
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
private fun Countdown(
    startTime: Instant,
    ticker: Flow<Unit>,
) {
    fun lessThanFiveMinutesRemaining(): Boolean {
        return Duration.between(Instant.now(), startTime).seconds < 300
    }

    var timeRemaining by remember {
        mutableStateOf(getTimeRemainingUntil(startTime))
    }
    var isStartingSoon by remember { mutableStateOf(lessThanFiveMinutesRemaining()) }

    val defaultBackgroundColor by rememberUpdatedState(
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
    )
    val urgentBackgroundColor by rememberUpdatedState(
        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
    )
    val textStyle by rememberUpdatedState(
        MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
    )

    // Update remaining time every second.
    LaunchedEffect(Unit) {
        ticker.collect {
            timeRemaining = getTimeRemainingUntil(startTime)
            isStartingSoon = lessThanFiveMinutesRemaining()
        }
    }

    Text(
        text = timeRemaining,
        modifier = Modifier
            .background(
                color = if (isStartingSoon)
                    urgentBackgroundColor else
                    defaultBackgroundColor,
                shape = RoundedCornerShape(100),
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        color = if (isStartingSoon)
            MaterialTheme.colorScheme.error else
            MaterialTheme.colorScheme.tertiary,
        style = textStyle,
    )
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


// region Previews

@Preview
@Composable
internal fun Preview_RaceCard(
    @PreviewParameter(RaceProvider::class)
    race: Race,
) {
    NexonTheme {
        RaceCard(
            race = race,
            ticker = emptyFlow(),
        )
    }
}

internal class RaceProvider :
    PreviewParameterProvider<Race> {
    override val values = sequenceOf(
        STANDARD_GREYHOUND,
        STANDARD_HARNESS,
        STANDARD_HORSE,
        STANDARD_UNKNOWN,
        LONG_NAME,
    )

    companion object {
        val STANDARD_GREYHOUND = Race(
            id = "greyhound",
            meetingName = "Greyhoundwick",
            raceNumber = 16,
            category = RacingCategory.GREYHOUND,
            startTime = Instant.ofEpochSecond(1740783000L),
        )
        val STANDARD_HARNESS = Race(
            id = "harness",
            meetingName = "Harnessville",
            raceNumber = 16,
            category = RacingCategory.HARNESS,
            startTime = Instant.ofEpochSecond(1740783000L),
        )
        val STANDARD_HORSE = Race(
            id = "horse",
            meetingName = "Horsewich",
            raceNumber = 16,
            category = RacingCategory.HORSE,
            startTime = Instant.ofEpochSecond(1740783000L),
        )
        val STANDARD_UNKNOWN = Race(
            id = "unknown",
            meetingName = "Unknown",
            raceNumber = 16,
            category = RacingCategory.UNKNOWN,
            startTime = Instant.ofEpochSecond(1740783000L),
        )
        val LONG_NAME = Race(
            id = "longName",
            meetingName = "Eagle Farm of the Shire of Middle Earth",
            raceNumber = 420,
            category = RacingCategory.HORSE,
            startTime = Instant.ofEpochSecond(1740783000L),
        )
    }
}

// endregion
