package com.fghilmany.nufitai.data.exerciselibrary.datasource

import com.fghilmany.nufitai.data.exerciselibrary.model.ExerciseSeedDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nufitai.shared.generated.resources.Res

/** Reads the bundled `files/exercises.json` asset (extracted from `Gym Techniques/`). */
class ExerciseLibrarySeedDataSource {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun readSeed(): List<ExerciseSeedDto> = withContext(Dispatchers.Default) {
        val bytes = Res.readBytes("files/exercises.json")
        json.decodeFromString<List<ExerciseSeedDto>>(bytes.decodeToString())
    }
}
