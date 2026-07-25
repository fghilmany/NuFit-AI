package com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel

import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.testExercise
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseAlternatives
import com.fghilmany.nufitai.usecase.exerciselibrary.GetExerciseDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseDetailViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repository: FakeExerciseLibraryRepository) = ExerciseDetailViewModel(
        getExerciseDetail = GetExerciseDetail(repository),
        getExerciseAlternatives = GetExerciseAlternatives(repository),
    )

    @Test
    fun `given an existing exercise id when loaded then state is Loaded`() = runTest {
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(testExercise(id = "BW-SQUAT-000")) }
        val vm = viewModel(repository)

        vm.load("BW-SQUAT-000")

        assertIs<ExerciseDetailState.Loaded>(vm.state.value)
    }

    @Test
    fun `given an unknown exercise id when loaded then state is Error, not a crash`() = runTest {
        val repository = FakeExerciseLibraryRepository()
        val vm = viewModel(repository)

        vm.load("does-not-exist")

        assertIs<ExerciseDetailState.Error>(vm.state.value)
    }
}
