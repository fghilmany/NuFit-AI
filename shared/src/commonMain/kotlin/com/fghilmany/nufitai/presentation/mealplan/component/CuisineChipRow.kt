package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** P-17 B "Tema Makanan Favorit" -- screenshot's chip set (Nusantara/Western/Sundanese/Javanese/Chinese/Japanese); free text per techspec §5, not a closed domain enum. */
val MEAL_PREFERENCE_CUISINE_OPTIONS = listOf("Nusantara", "Western", "Sundanese", "Javanese", "Chinese", "Japanese")

/** Horizontally scrollable, mirrors `SuggestedReplyRow`'s existing LazyRow pattern (chat-ai.md). */
@Composable
fun CuisineChipRow(selected: Set<String>, onToggle: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(MEAL_PREFERENCE_CUISINE_OPTIONS) { theme ->
            FilterChip(selected = theme in selected, onClick = { onToggle(theme) }, label = { Text(theme) })
        }
    }
}
