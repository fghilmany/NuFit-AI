package com.fghilmany.nufitai.presentation.exerciselibrary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.presentation.exerciselibrary.component.ExerciseCard
import com.fghilmany.nufitai.presentation.exerciselibrary.component.FilterChipRow
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseLibraryEvent
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseLibraryState
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseLibraryViewModel
import com.fghilmany.nufitai.usecase.exerciselibrary.ExerciseFilter
import org.koin.compose.viewmodel.koinViewModel

/** P-07 (issue #79, Figma node 12:683). */
@Composable
fun ExerciseLibraryScreen(
    onBack: () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    viewModel: ExerciseLibraryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(onBack = onBack)

        when (val current = state) {
            ExerciseLibraryState.Loading -> LoadingBox()
            is ExerciseLibraryState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is ExerciseLibraryState.Loaded -> LibraryContent(
                exercises = current.exercises,
                query = current.query,
                filter = current.filter,
                viewModel = viewModel,
                onExerciseClick = onExerciseClick,
            )
            is ExerciseLibraryState.Empty -> EmptyContent(
                query = current.query,
                filter = current.filter,
                hasActiveFilters = current.hasActiveFilters,
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun LibraryContent(
    exercises: List<Exercise>,
    query: String,
    filter: ExerciseFilter,
    viewModel: ExerciseLibraryViewModel,
    onExerciseClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { SearchAndFilters(query, filter, viewModel) }

        val rows = exercises.chunked(2)
        rows.forEach { pair ->
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEach { exercise ->
                        ExerciseCard(exercise, onClick = { onExerciseClick(exercise.id) }, modifier = Modifier.weight(1f))
                    }
                    if (pair.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        }

        item { AiSuggestionBanner() }
    }
}

@Composable
private fun EmptyContent(
    query: String,
    filter: ExerciseFilter,
    hasActiveFilters: Boolean,
    viewModel: ExerciseLibraryViewModel,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        SearchAndFilters(query, filter, viewModel, modifier = Modifier.padding(top = 16.dp))
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gerakan tidak ditemukan", style = MaterialTheme.typography.titleMedium, color = NuFitColors.Primary)
                if (hasActiveFilters) {
                    Text(
                        "Coba hapus filter atau kata kunci pencarian.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Hapus Filter",
                        style = MaterialTheme.typography.labelLarge,
                        color = NuFitColors.Primary,
                        modifier = Modifier.padding(top = 8.dp).clickable { viewModel.onEvent(ExerciseLibraryEvent.ClearFilters) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    query: String,
    filter: ExerciseFilter,
    viewModel: ExerciseLibraryViewModel,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            value = query,
            onValueChange = { viewModel.onEvent(ExerciseLibraryEvent.SearchQueryChanged(it)) },
            placeholder = "Cari gerakan...",
            modifier = Modifier.fillMaxWidth(),
        )
        FilterChipRow(
            filter = filter,
            onToggleMuscleGroup = { viewModel.onEvent(ExerciseLibraryEvent.ToggleMuscleGroup(it)) },
            onToggleEquipment = { viewModel.onEvent(ExerciseLibraryEvent.ToggleEquipment(it)) },
            onToggleLevel = { viewModel.onEvent(ExerciseLibraryEvent.ToggleLevel(it)) },
        )
    }
}

/** "Butuh Saran?" -- Chat AI target doesn't exist yet, inert per issue #79 §9 item 9. */
@Composable
private fun AiSuggestionBanner() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.Primary,
        contentPadding = PaddingValues(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Butuh Saran?", style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(
                    "Tanyakan NuFit AI gerakan yang cocok untukmu hari ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NuFitColors.InversePrimary,
                )
            }
            Box(
                modifier = Modifier.size(48.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = NuFitColors.Primary)
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text("Jelajahi Gerakan", style = MaterialTheme.typography.titleMedium, color = NuFitColors.Primary)
        }
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(NuFitColors.PrimaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = NuFitColors.OnPrimaryContainer)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
