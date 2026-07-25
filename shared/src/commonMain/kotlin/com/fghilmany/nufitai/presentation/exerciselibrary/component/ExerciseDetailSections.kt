package com.fghilmany.nufitai.presentation.exerciselibrary.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.CommonMistake
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise

/** P-08 "Otot Target" chip row (issue #79, Figma node 12:835). */
@Composable
fun TargetMuscleChips(targetMuscles: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        targetMuscles.forEach { muscle ->
            AppCard(
                backgroundColor = NuFitColors.SecondaryContainer,
                shape = RoundedCornerShape(percent = 50),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(muscle, style = MaterialTheme.typography.labelLarge, color = NuFitColors.OnSecondaryContainer)
            }
        }
    }
}

/** P-08 "Instruksi Langkah-demi-Langkah" (issue #79). */
@Composable
fun InstructionsCard(instructions: List<String>, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth(), contentPadding = PaddingValues(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Instruksi Langkah-demi-Langkah", style = MaterialTheme.typography.titleLarge, color = NuFitColors.Primary)
            instructions.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(24.dp).background(NuFitColors.Primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/** P-08 "Hindari Hal Ini" (issue #79). */
@Composable
fun CommonMistakesCard(mistakes: List<CommonMistake>, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.errorContainer,
        contentPadding = PaddingValues(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Hindari Hal Ini", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            }
            mistakes.forEach { mistake ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(mistake.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text(mistake.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

/** P-08 "Saran NuFit AI" (issue #79). */
@Composable
fun SafetyTipsCard(tips: List<String>, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.TertiaryFixed,
        contentPadding = PaddingValues(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Saran NuFit AI", style = MaterialTheme.typography.titleMedium, color = NuFitColors.OnTertiaryFixed)
            tips.forEach { tip ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = NuFitColors.OnTertiaryFixed, modifier = Modifier.size(20.dp))
                    Text(tip, style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnTertiaryFixed)
                }
            }
        }
    }
}

/** P-08 "Gerakan Alternatif" (issue #79 §5, computed heuristic -- see GetExerciseAlternatives). */
@Composable
fun AlternativesRow(alternatives: List<Exercise>, onAlternativeClick: (String) -> Unit, modifier: Modifier = Modifier) {
    if (alternatives.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Gerakan Alternatif", style = MaterialTheme.typography.titleLarge, color = NuFitColors.Primary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(alternatives) { alternative ->
                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable { onAlternativeClick(alternative.id) },
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(NuFitColors.SurfaceContainerHigh, RoundedCornerShape(16.dp)),
                    )
                    Text(alternative.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = NuFitColors.Primary)
                }
            }
        }
    }
}
