package com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel

import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.FilterExercises
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExercisePool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseLibraryViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val squat = testExercise(id = "BW-SQUAT-000", equipmentCategory = EquipmentCategory.BODYWEIGHT, primaryMuscleGroup = MuscleGroup.KAKI)
        .copy(name = "Bodyweight Squat")
    private val pushup = testExercise(id = "BW-PUSHH-000", equipmentCategory = EquipmentCategory.BODYWEIGHT, movementPattern = MovementPattern.PUSH_HORIZONTAL, primaryMuscleGroup = MuscleGroup.DADA)
        .copy(name = "Push-up")

    private fun viewModel(): ExerciseLibraryViewModel {
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(squat, pushup) }
        return ExerciseLibraryViewModel(GetExercisePool(repository), FilterExercises())
    }

    @Test
    fun `given a loaded pool when no filter then state is Loaded with all exercises`() = runTest {
        val vm = viewModel()

        val state = vm.state.value
        assertIs<ExerciseLibraryState.Loaded>(state)
        assertEquals(2, state.exercises.size)
    }

    @Test
    fun `given a search query when it matches nothing then state is Empty with hasActiveFilters true`() = runTest {
        val vm = viewModel()

        vm.onEvent(ExerciseLibraryEvent.SearchQueryChanged("nonexistent"))

        val state = vm.state.value
        assertIs<ExerciseLibraryState.Empty>(state)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `given a muscle group filter when applied then only matching exercises remain`() = runTest {
        val vm = viewModel()

        vm.onEvent(ExerciseLibraryEvent.ToggleMuscleGroup(MuscleGroup.DADA))

        val state = vm.state.value
        assertIs<ExerciseLibraryState.Loaded>(state)
        assertEquals(listOf("BW-PUSHH-000"), state.exercises.map { it.id })
    }

    @Test
    fun `given active filters when cleared then full pool is restored`() = runTest {
        val vm = viewModel()
        vm.onEvent(ExerciseLibraryEvent.ToggleMuscleGroup(MuscleGroup.DADA))

        vm.onEvent(ExerciseLibraryEvent.ClearFilters)

        val state = vm.state.value
        assertIs<ExerciseLibraryState.Loaded>(state)
        assertEquals(2, state.exercises.size)
    }
}
