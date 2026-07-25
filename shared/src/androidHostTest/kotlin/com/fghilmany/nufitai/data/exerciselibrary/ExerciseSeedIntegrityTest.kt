package com.fghilmany.nufitai.data.exerciselibrary

import com.fghilmany.nufitai.data.exerciselibrary.model.ExerciseSeedDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * AC-5 (BLOCKING, issue #79): every exercise must ship non-empty `commonMistakes`/`safetyTips`.
 * JVM-only (androidHostTest, like DiVerificationTest) -- reads the bundled seed file directly
 * off disk rather than through Compose resources, which aren't loadable from a plain unit test.
 */
class ExerciseSeedIntegrityTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun readSeed(): List<ExerciseSeedDto> {
        val file = File("src/commonMain/composeResources/files/exercises.json")
        check(file.exists()) { "Expected bundled seed at ${file.absolutePath}" }
        return json.decodeFromString<List<ExerciseSeedDto>>(file.readText())
    }

    @Test
    fun `given the bundled seed when read then every exercise has 135 entries`() {
        val seed = readSeed()
        assertTrue(seed.size == 135, "Expected 135 exercises, found ${seed.size}")
    }

    @Test
    fun `given the bundled seed when read then every exercise has non-empty common mistakes`() {
        val seed = readSeed()
        val missing = seed.filter { it.commonMistakes.isEmpty() }.map { it.id }
        assertTrue(missing.isEmpty(), "Exercises with empty commonMistakes: $missing")
    }

    @Test
    fun `given the bundled seed when read then every exercise has non-empty safety tips`() {
        val seed = readSeed()
        val missing = seed.filter { it.safetyTips.isEmpty() }.map { it.id }
        assertTrue(missing.isEmpty(), "Exercises with empty safetyTips: $missing")
    }

    @Test
    fun `given the bundled seed when read then every exercise has a primary muscle group`() {
        val seed = readSeed()
        val invalid = seed.filterNot { dto ->
            runCatching { com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup.valueOf(dto.primaryMuscleGroup) }.isSuccess
        }.map { it.id }
        assertTrue(invalid.isEmpty(), "Exercises with invalid primaryMuscleGroup: $invalid")
    }
}
