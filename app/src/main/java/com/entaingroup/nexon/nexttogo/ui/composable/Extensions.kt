package com.entaingroup.nexon.nexttogo.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.entaingroup.nexon.R
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory

@Composable
internal fun RacingCategory.getReadableName(): String {
    val stringId = when (this) {
        RacingCategory.GREYHOUND -> R.string.racing_category_greyhound
        RacingCategory.HARNESS -> R.string.racing_category_harness
        RacingCategory.HORSE -> R.string.racing_category_horse
        RacingCategory.UNKNOWN -> R.string.racing_category_unknown
    }
    return stringResource(stringId)
}
