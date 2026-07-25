package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.SplashState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.SplashViewModel
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SplashScreen(
    onRouted: (hasLocalProfile: Boolean) -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val routed = state as? SplashState.Routed ?: return@LaunchedEffect
        onRouted(routed.hasLocalProfile)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Decorative blurred blobs (DESIGN.md: soft, tactile, non-threatening shapes).
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .size(256.dp)
                .blur(80.dp)
                .background(NuFitColors.PrimaryContainer.copy(alpha = 0.25f), CircleShape),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-60).dp, y = 60.dp)
                .size(256.dp)
                .blur(80.dp)
                .background(NuFitColors.SecondaryContainer.copy(alpha = 0.5f), CircleShape),
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(128.dp).background(NuFitColors.Primary, RoundedCornerShape(32.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NuFit AI", style = MaterialTheme.typography.headlineLarge, color = NuFitColors.Primary)
                Text(
                    "Latihan cerdas, hasil nyata",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator(color = NuFitColors.Primary, modifier = Modifier.size(28.dp))
            Text(
                "Menyiapkan pengalaman Anda...",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
