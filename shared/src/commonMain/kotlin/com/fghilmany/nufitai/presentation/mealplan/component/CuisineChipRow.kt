package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/** P-17 B "Tema Makanan Favorit" -- screenshot's chip set (Nusantara/Western/Sundanese/Javanese/Chinese/Japanese); free text per techspec §5, not a closed domain enum.
 * "Bebas" (dev-requested, 19 Nov 2026) is the explicit no-preference option -- mutually exclusive
 * with every named theme, handled in MealPreferenceViewModel.onToggleCuisineTheme. */
const val CUISINE_THEME_FREE = "Bebas"
val MEAL_PREFERENCE_CUISINE_OPTIONS = listOf("Nusantara", "Western", "Sundanese", "Javanese", "Chinese", "Japanese", CUISINE_THEME_FREE)

/** Horizontally scrollable, mirrors `SuggestedReplyRow`'s existing LazyRow pattern (chat-ai.md).
 * Edge fade (dev-requested, 19 Nov 2026): the row has no scrollbar and the last chip used to get
 * cut off flush against the screen edge with no hint more options exist off-screen. */
@Composable
fun CuisineChipRow(selected: Set<String>, onToggle: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(MEAL_PREFERENCE_CUISINE_OPTIONS) { theme ->
                FilterChip(selected = theme in selected, onClick = { onToggle(theme) }, label = { Text(theme) })
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(24.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(MaterialTheme.colorScheme.background.copy(alpha = 0f), MaterialTheme.colorScheme.background),
                    ),
                ),
        )
    }
}
