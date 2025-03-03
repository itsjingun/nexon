package com.entaingroup.nexon.nexttogo.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.entaingroup.nexon.nexttogo.domain.model.RacingCategory
import com.entaingroup.nexon.nexttogo.ui.NextToGoRacesContract.Companion.DEFAULT_CATEGORIES
import com.entaingroup.nexon.ui.theme.NexonTheme

@Composable
internal fun CategoryFilterList(
    categories: Set<RacingCategory>,
    selectedCategories: Set<RacingCategory>,
    onCategoryChipClick: (RacingCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = categories.toList(), key = { it.id }) { category ->
            val selected = selectedCategories.contains(category)
            FilterChip(
                selected = selected,
                onClick = { onCategoryChipClick(category) },
                label = { Text(category.getReadableName()) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

// region Previews

@Preview(showBackground = true)
@Composable
internal fun Preview_CategoryFilterList() {
    NexonTheme {
        CategoryFilterList(
            categories = DEFAULT_CATEGORIES,
            selectedCategories = emptySet(),
            onCategoryChipClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun Preview_CategoryFilterList_PartiallySelected() {
    NexonTheme {
        CategoryFilterList(
            categories = DEFAULT_CATEGORIES,
            selectedCategories = setOf(RacingCategory.HARNESS),
            onCategoryChipClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
internal fun Preview_CategoryFilterList_AllSelected() {
    NexonTheme {
        CategoryFilterList(
            categories = DEFAULT_CATEGORIES,
            selectedCategories = DEFAULT_CATEGORIES,
            onCategoryChipClick = {},
        )
    }
}

// endregion
