package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository

class FakeExerciseLibraryRepository : ExerciseLibraryRepository {
    var exercises: List<Exercise> = emptyList()
    var ensureSeededCallCount: Int = 0
        private set

    override suspend fun ensureSeeded(): AppResult<Unit> {
        ensureSeededCallCount++
        return AppResult.Success(Unit)
    }

    override suspend fun getAll(): AppResult<List<Exercise>> = AppResult.Success(exercises)

    override suspend fun getById(id: String): AppResult<Exercise?> =
        AppResult.Success(exercises.find { it.id == id })
}
