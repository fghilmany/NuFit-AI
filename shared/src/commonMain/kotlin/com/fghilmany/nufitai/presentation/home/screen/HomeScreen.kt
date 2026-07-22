package com.fghilmany.nufitai.presentation.home.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Stub only. Home / Monthly Plan (P-03) is a separate feature/techspec that hasn't been
 * built yet -- this exists purely so onboarding navigation has a real destination to land on.
 */
@Composable
fun HomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home (Monthly Plan) -- belum dibangun", style = MaterialTheme.typography.bodyLarge)
    }
}
