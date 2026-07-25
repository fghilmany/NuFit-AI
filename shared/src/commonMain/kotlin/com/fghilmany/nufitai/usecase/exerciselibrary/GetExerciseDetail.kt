package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository

/** P-08 (issue #79). */
class GetExerciseDetail(private val repository: ExerciseLibraryRepository) {
    suspend operator fun invoke(exerciseId: String): AppResult<Exercise?> = repository.getById(exerciseId)
}
