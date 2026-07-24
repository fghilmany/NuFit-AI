package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.CardioBlock
import com.fghilmany.nufitai.domain.monthlyplan.entity.CooldownBlock
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.monthlyplan.entity.WarmupBlock

data class Fase(val nama: String, val setCount: Int, val rpeRange: IntRange)

private val TEMPLATE_ORDER = listOf("A", "B", "C")

/** issue #29 layer 6 + WARM/COOL rules (WARM-01..03, COOL-01..03). Pure, no I/O. */
object PenjadwalanBulan {

    /** A -> squat/hinge/push_h/pull_h/core, B -> push_v instead of push_h, C -> pull_v instead of pull_h. */
    val TEMPLATE_ROTATION: Map<String, List<MovementPattern>> = mapOf(
        "A" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE),
        "B" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE),
        "C" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE),
    )

    /** Layer 6 "kerangka": floor(frekuensi*30/7) sessions, spread over `hariPilihan`, min 1 rest day between for beginners. */
    fun buildKerangkaKalender(frekuensiPerMinggu: Int, hariPilihan: Set<Int>, minRestBetweenSessions: Boolean): List<Int> {
        val totalSesi = (frekuensiPerMinggu * 30) / 7 // floor, per issue #29's spec (not issue #18's "round")
        if (hariPilihan.isEmpty() || totalSesi <= 0) return emptyList()
        val sortedHari = hariPilihan.sorted()
        val sessionDays = mutableListOf<Int>()
        var day = 1
        var lastSessionDay = -100
        while (day <= 30 && sessionDays.size < totalSesi) {
            val isPreferredWeekday = ((day - 1) % 7) + 1 in sortedHari
            val respectsRest = !minRestBetweenSessions || (day - lastSessionDay) > 1
            if (isPreferredWeekday && respectsRest) {
                sessionDays.add(day)
                lastSessionDay = day
            }
            day++
        }
        return sessionDays
    }

    fun templateFor(sessionIndex: Int): String = TEMPLATE_ORDER[sessionIndex % TEMPLATE_ORDER.size]

    /** Layer 6 "fase" + `override_konservatif`. */
    fun faseFor(day: Int, mode: ProgressionMode): Fase {
        val adaptasiEnd = if (mode == ProgressionMode.KONSERVATIF) 21 else 14
        val progresiEnd = if (mode == ProgressionMode.KONSERVATIF) 28 else 28
        return when {
            day <= adaptasiEnd -> Fase("adaptasi", setCount = 2, rpeRange = 4..5)
            day <= progresiEnd -> Fase("progresi", setCount = 3, rpeRange = 6..7)
            else -> Fase("buffer", setCount = 2, rpeRange = 4..5)
        }
    }

    /** WARM-01: cardio pool entry (or a no-equipment placeholder if none available), 3-5 minutes. */
    fun warmupUmum(pool: Map<MovementPattern, List<Exercise>>): CardioBlock {
        val cardio = pool[MovementPattern.CARDIO]?.firstOrNull()
        return CardioBlock(jenis = cardio?.name ?: "Jalan di tempat", durasiMenit = 4, intensitas = "sangat ringan")
    }

    /** COOL-01: mandatory for HEALTH_JANTUNG/HEALTH_TEKANAN_DARAH -- caller must never truncate this for those flags. */
    fun cooldownPenurunanHr(): CardioBlock = CardioBlock(jenis = "Jalan pelan", durasiMenit = 3, intensitas = "sangat ringan")

    /** WARM-02: one lightest-regresi set per pattern in this session, teaching-rep dose. */
    fun warmupSpesifik(sessionPatterns: List<MovementPattern>, pool: Map<MovementPattern, List<Exercise>>): List<PlannedExercise> =
        sessionPatterns.mapNotNull { pattern ->
            pool[pattern]?.firstOrNull()?.let { exercise ->
                PlannedExercise(exercise.id, sets = 1, repRangeOrDuration = "8-10 rep", rpeTargetMin = 3, rpeTargetMax = 4, reasonRuleIds = listOf("WARM-02"))
            }
        }

    /** COOL-02: 2-3 stretches whose `polaGerakTerkait` overlaps this session's patterns, deduped against `korektifWajib` (COOL-03). */
    fun cooldownStretch(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        korektifWajib: List<Exercise>,
    ): List<PlannedExercise> {
        val korektifIds = korektifWajib.map { it.id }.toSet()
        return pool[MovementPattern.STRETCH].orEmpty()
            .filter { it.id !in korektifIds && it.polaGerakTerkait?.any { p -> p in sessionPatterns } == true }
            .take(3)
            .map { PlannedExercise(it.id, sets = 1, repRangeOrDuration = "20-30 detik", rpeTargetMin = 2, rpeTargetMax = 3, reasonRuleIds = listOf("COOL-02")) }
    }

    fun toWarmupBlock(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        korektifWajib: List<Exercise>,
    ): WarmupBlock = WarmupBlock(
        umum = warmupUmum(pool),
        spesifik = warmupSpesifik(sessionPatterns, pool),
        korektif = korektifWajib.map {
            PlannedExercise(it.id, sets = 2, repRangeOrDuration = "10 rep", rpeTargetMin = 3, rpeTargetMax = 4, reasonRuleIds = listOf("SAFE-09", "WARM-03"))
        },
    )

    fun toCooldownBlock(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        korektifWajib: List<Exercise>,
    ): CooldownBlock = CooldownBlock(
        penurunanHr = cooldownPenurunanHr(),
        stretch = cooldownStretch(sessionPatterns, pool, korektifWajib),
    )
}
