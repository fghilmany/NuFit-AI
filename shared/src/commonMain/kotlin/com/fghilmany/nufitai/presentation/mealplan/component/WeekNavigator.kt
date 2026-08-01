package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_this_week
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Screenshot §2 (meal-plan.md) -- `< 15 – 21 Mei >` navigator. Rendered as `d/M` (day/month
 * numbers) rather than a localized month name -- no month-name formatter exists anywhere in this
 * codebase to mirror (only HH:mm formatting has precedent, see `AccountScreen`); a deliberate
 * simplification, not a design match.
 */
@Composable
fun WeekNavigator(weekStartDate: Instant, isCurrentWeek: Boolean, canGoNext: Boolean, onPrev: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val timeZone = TimeZone.currentSystemDefault()
    val start = weekStartDate.toLocalDateTime(timeZone).date
    val end = (weekStartDate + 6.days).toLocalDateTime(timeZone).date

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) { Icon(Icons.Filled.ChevronLeft, contentDescription = null) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${start.dayOfMonth}/${start.monthNumber} – ${end.dayOfMonth}/${end.monthNumber}", style = MaterialTheme.typography.titleSmall)
            if (isCurrentWeek) {
                Text(stringResource(Res.string.mealplan_this_week), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = onNext, enabled = canGoNext) { Icon(Icons.Filled.ChevronRight, contentDescription = null) }
    }
}
