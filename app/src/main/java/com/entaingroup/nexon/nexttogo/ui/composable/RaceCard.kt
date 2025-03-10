package com.entaingroup.nexon.nexttogo.ui.composable

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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.R
import com.entaingroup.nexon.nexttogo.domain.TimeProvider
import com.entaingroup.nexon.nexttogo.domain.model.Race
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.ui.theme.NexonTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Duration
import java.time.Instant
import kotlin.math.abs

@Composable
internal fun RaceCard(
    race: Race,
    timeProvider: TimeProvider,
    ticker: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .then(modifier),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(category = race.category)
            Spacer(modifier = Modifier.width(16.dp))
            Title(meetingName = race.meetingName, raceNumber = race.raceNumber)
            Spacer(modifier = Modifier.width(16.dp))
            Countdown(startTime = race.startTime, timeProvider = timeProvider, ticker = ticker)
        }
    }
}

@Composable
private fun CategoryIcon(category: RacingCategory) {
    val fontScale = LocalConfiguration.current.fontScale

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
            .size(40.dp * fontScale)
            .clip(CircleShape)
            .background(color = MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = stringResource(contentDescriptionId),
            modifier = Modifier.size(28.dp * fontScale),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RowScope.Title(meetingName: String, raceNumber: Int) {
    Text(
        text = buildAnnotatedString {
            append(meetingName.trim())
            append(" ")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Normal,
                ),
            ) {
                append("R$raceNumber")
            }
        },
        modifier = Modifier
            .weight(1.0f)
            .semantics { contentDescription = "$meetingName, race $raceNumber" },
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
private fun Countdown(startTime: Instant, timeProvider: TimeProvider, ticker: Flow<Unit>) {
    fun lessThanFiveMinutesRemaining(): Boolean {
        return Duration.between(timeProvider.now(), startTime).seconds < 300
    }

    val (text, description) = getTimeRemainingUntil(timeProvider.now(), startTime)
    var timeRemaining by remember { mutableStateOf(text) }
    var timeRemainingDescription by remember { mutableStateOf(description) }
    var isStartingSoon by remember { mutableStateOf(lessThanFiveMinutesRemaining()) }

    val defaultBackgroundColor by rememberUpdatedState(
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
    )
    val urgentBackgroundColor by rememberUpdatedState(
        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
    )
    val textStyle by rememberUpdatedState(
        MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Update remaining time every second.
    LaunchedEffect(Unit) {
        ticker.collect {
            val (t, d) = getTimeRemainingUntil(timeProvider.now(), startTime)
            timeRemaining = t
            timeRemainingDescription = d
            isStartingSoon = lessThanFiveMinutesRemaining()
        }
    }

    // Set to "urgent" colors with there are less than five minutes remaining.
    Text(
        text = timeRemaining,
        modifier = Modifier
            .background(
                color = if (isStartingSoon) urgentBackgroundColor else defaultBackgroundColor,
                shape = RoundedCornerShape(100),
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .semantics { contentDescription = timeRemainingDescription },
        color = if (isStartingSoon) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.tertiary
        },
        style = textStyle,
    )
}

/**
 * Returns a pair with the time remaining until an [Instant], e.g.:
 *
 * - 5h 9m 4s | 5 hours 9 minutes 4 seconds remaining
 * - 24m 48s | 24 minutes 48 seconds remaining
 * - 0s | 0 seconds remaining
 * - -42s | Minus 42 seconds remaining
 *
 * @return pair where first value is the visual representation, and second value is the content
 * description for accessibility.
 */
internal fun getTimeRemainingUntil(now: Instant, then: Instant): Pair<String, String> {
    val duration = Duration.between(now, then)
    val isNegative = duration.seconds < 0
    val diff = abs(duration.seconds)

    val hours = diff / 3600
    val minutes = (diff % 3600) / 60
    val seconds = diff % 60

    val parts = mutableListOf<String>()
    val descriptionParts = mutableListOf<String>()

    if (hours > 0) {
        parts.add("${hours}h")
        descriptionParts.add("$hours hour" + if (hours != 1L) "s" else "")
    }
    if (minutes > 0) {
        parts.add("${minutes}m")
        descriptionParts.add("$minutes minute" + if (minutes != 1L) "s" else "")
    }
    if (seconds > 0 || (hours == 0L && minutes == 0L)) {
        parts.add("${seconds}s")
        descriptionParts.add("$seconds second" + if (seconds != 1L) "s" else "")
    }

    val text = (if (isNegative) "-" else "") + parts.joinToString(" ")
    val description =
        (if (isNegative) "Minus " else "") + "${descriptionParts.joinToString(" ")} remaining"

    return Pair(text, description)
}

// region Previews

@Preview
@PreviewFontScale
@Composable
internal fun Preview_RaceCard(
    @PreviewParameter(RaceProvider::class)
    race: Race,
) {
    NexonTheme {
        RaceCard(
            race = race,
            timeProvider = object : TimeProvider {
                override fun now() = Instant.EPOCH
            },
            ticker = emptyFlow(),
        )
    }
}

internal class RaceProvider :
    PreviewParameterProvider<Race> {
    override val values = sequenceOf(
        GREYHOUND_STARTING_SOON,
        GREYHOUND_JUST_STARTED,
        GREYHOUND_ALREADY_STARTED,
        HARNESS_FIVE_MINUTES_LEFT,
        HORSE_MINUTES_AND_SECONDS,
        UNKNOWN_HOURS_AND_MINUTES_AND_SECONDS,
        LONG_NAME,
    )

    companion object {
        val GREYHOUND_STARTING_SOON = Race(
            id = "greyhound",
            meetingName = "Greyhoundwick",
            raceNumber = 16,
            category = RacingCategory.GREYHOUND,
            startTime = Instant.ofEpochSecond(299),
        )
        val GREYHOUND_JUST_STARTED = Race(
            id = "greyhound",
            meetingName = "Greyhoundwick",
            raceNumber = 16,
            category = RacingCategory.GREYHOUND,
            startTime = Instant.EPOCH,
        )
        val GREYHOUND_ALREADY_STARTED = Race(
            id = "greyhound",
            meetingName = "Greyhoundwick",
            raceNumber = 16,
            category = RacingCategory.GREYHOUND,
            startTime = Instant.ofEpochSecond(-42),
        )
        val HARNESS_FIVE_MINUTES_LEFT = Race(
            id = "harness",
            meetingName = "Harnessville",
            raceNumber = 16,
            category = RacingCategory.HARNESS,
            startTime = Instant.ofEpochSecond(300),
        )
        val HORSE_MINUTES_AND_SECONDS = Race(
            id = "horse",
            meetingName = "Horsewich",
            raceNumber = 16,
            category = RacingCategory.HORSE,
            startTime = Instant.ofEpochSecond(642),
        )
        val UNKNOWN_HOURS_AND_MINUTES_AND_SECONDS = Race(
            id = "unknown",
            meetingName = "Unknown",
            raceNumber = 16,
            category = RacingCategory.UNKNOWN,
            startTime = Instant.ofEpochSecond(3852),
        )
        val LONG_NAME = Race(
            id = "longName",
            meetingName = "Eagle Farm of the Shire of Middle Earth",
            raceNumber = 420,
            category = RacingCategory.HORSE,
            startTime = Instant.ofEpochSecond(425),
        )
    }
}

// endregion
