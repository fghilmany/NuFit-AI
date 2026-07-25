package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.fake.FakeExerciseLibraryRepository
import com.fghilmany.nufitai.fake.testExercise
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GetExerciseDetailTest {

    @Test
    fun `given an existing id when invoked then returns the exercise`() = runTest {
        val repository = FakeExerciseLibraryRepository().apply { exercises = listOf(testExercise(id = "BW-SQUAT-000")) }
        val getExerciseDetail = GetExerciseDetail(repository)

        val result = getExerciseDetail("BW-SQUAT-000")

        assertIs<AppResult.Success<*>>(result)
        assertEquals("BW-SQUAT-000", result.data?.id)
    }

    @Test
    fun `given an unknown id when invoked then returns null, not an error`() = runTest {
        val repository = FakeExerciseLibraryRepository()
        val getExerciseDetail = GetExerciseDetail(repository)

        val result = getExerciseDetail("does-not-exist")

        assertIs<AppResult.Success<*>>(result)
        assertNull(result.data)
    }
}
