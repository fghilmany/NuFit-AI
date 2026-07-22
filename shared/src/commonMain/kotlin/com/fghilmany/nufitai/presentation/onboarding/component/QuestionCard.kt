package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** PAR-Q question card: text + Ya/Tidak buttons, matching the "Cek Kesehatan Dulu" Figma pattern (node 12:26). */
@Composable
fun QuestionCard(
    questionNumber: Int,
    questionText: String,
    selected: Boolean?,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Text(
            text = "$questionNumber. $questionText",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 25.dp, end = 25.dp, top = 25.dp, bottom = 16.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 25.dp, end = 25.dp, bottom = 25.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnswerButton(
                label = "Ya",
                isSelected = selected == true,
                onClick = { onAnswer(true) },
                modifier = Modifier.weight(1f),
            )
            AnswerButton(
                label = "Tidak",
                isSelected = selected == false,
                onClick = { onAnswer(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RowScope.AnswerButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        colors = if (isSelected) {
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        },
    ) {
        Text(label)
    }
}
