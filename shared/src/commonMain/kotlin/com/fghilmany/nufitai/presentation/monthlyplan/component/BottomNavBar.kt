package com.fghilmany.nufitai.presentation.monthlyplan.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.ui.theme.NuFitColors

private enum class NavTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    RENCANA("Rencana", Icons.Filled.ListAlt),
    CHAT_AI("Chat AI", Icons.Filled.Forum),
    MAKAN("Makan", Icons.Filled.Restaurant),
    AKUN("Akun", Icons.Filled.Person),
}

/**
 * Figma node 12:654 -- 5 tabs (Keputusan #12). Only Home is a real destination today; the other
 * 4 tabs' features (Plans, Chat AI, Meal Plan, Akun) aren't built yet, so they're inert for now
 * rather than navigating to a stub -- avoids fabricating a "coming soon" screen not in this pass's scope.
 */
@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        NavTab.entries.forEach { tab -> NavTabItem(tab, isActive = tab == NavTab.HOME) }
    }
}

@Composable
private fun NavTabItem(tab: NavTab, isActive: Boolean) {
    Column(
        modifier = Modifier
            .clickable(enabled = isActive) {}
            .background(if (isActive) NuFitColors.SecondaryContainer else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(percent = 50))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            tab.icon,
            contentDescription = tab.label,
            tint = if (isActive) NuFitColors.OnSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) NuFitColors.OnSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
