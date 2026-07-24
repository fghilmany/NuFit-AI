package com.fghilmany.nufitai.domain.exerciselibrary.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise

interface ExerciseLibraryRepository {
    /** Seeds from the bundled catalog asset on first launch if the local table is empty. */
    suspend fun ensureSeeded(): AppResult<Unit>

    suspend fun getAll(): AppResult<List<Exercise>>
    suspend fun getById(id: String): AppResult<Exercise?>
}
