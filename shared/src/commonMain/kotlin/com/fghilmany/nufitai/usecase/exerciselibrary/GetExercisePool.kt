package com.fghilmany.nufitai.usecase.exerciselibrary

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.repository.ExerciseLibraryRepository

/**
 * Ensures the library is seeded, then returns the full catalog. Filtering by
 * equipment/flags (POOL-01..05, SAFE-01..18) is the rule engine's job
 * (`usecase/monthlyplan/rules/`), not this usecase's -- both tiers share this
 * one unfiltered read.
 */
class GetExercisePool(private val repository: ExerciseLibraryRepository) {
    suspend operator fun invoke(): AppResult<List<Exercise>> {
        when (val seeded = repository.ensureSeeded()) {
            is AppResult.Error -> return seeded
            is AppResult.Success -> Unit
        }
        return repository.getAll()
    }
}
