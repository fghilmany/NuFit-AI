package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.monthlyplan.entity.CardioBlock
import com.fghilmany.nufitai.domain.monthlyplan.entity.CooldownBlock
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import com.fghilmany.nufitai.domain.monthlyplan.entity.WarmupBlock

data class Phase(val phaseName: String, val setCount: Int, val rpeRange: IntRange)

private val TEMPLATE_ORDER = listOf("A", "B", "C")

/** issue #29 layer 6 + WARM/COOL rules (WARM-01..03, COOL-01..03). Pure, no I/O. */
object MonthlyScheduler {

    /** A -> squat/hinge/push_h/pull_h/core, B -> push_v instead of push_h, C -> pull_v instead of pull_h. */
    val TEMPLATE_ROTATION: Map<String, List<MovementPattern>> = mapOf(
        "A" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE),
        "B" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_VERTICAL, MovementPattern.PULL_HORIZONTAL, MovementPattern.CORE),
        "C" to listOf(MovementPattern.SQUAT, MovementPattern.HINGE, MovementPattern.PUSH_HORIZONTAL, MovementPattern.PULL_VERTICAL, MovementPattern.CORE),
    )

    /**
     * Layer 6 "kerangka": floor(frekuensi*30/7) sessions, spread over `selectedWeekdays`, min 1 rest
     * day between for beginners. Guarantees exactly `totalSessions` days are returned (when
     * `selectedWeekdays` is non-empty) via two fallback passes -- previously, a `selectedWeekdays` whose
     * weekdays didn't leave room for the rest-gap (e.g. adjacent-day picks, or too few distinct
     * weekdays for the requested frequency) silently under-delivered sessions with no
     * correction, which is what produced e.g. a 4-5x/week pick collapsing to ~2x/week.
     */
    fun buildCalendarFramework(sessionsPerWeek: Int, selectedWeekdays: Set<Int>, minRestBetweenSessions: Boolean): List<Int> {
        val totalSessions = (sessionsPerWeek * 30) / 7 // floor, per issue #29's spec (not issue #18's "round")
        if (selectedWeekdays.isEmpty() || totalSessions <= 0) return emptyList()
        val sortedWeekdays = selectedWeekdays.sorted()

        // Pass 1: preferred weekdays, respecting the rest-gap if requested.
        val sessionDays = mutableListOf<Int>()
        var lastSessionDay = -100
        for (day in 1..30) {
            if (sessionDays.size >= totalSessions) break
            val isPreferredWeekday = ((day - 1) % 7) + 1 in sortedWeekdays
            val respectsRest = !minRestBetweenSessions || (day - lastSessionDay) > 1
            if (isPreferredWeekday && respectsRest) {
                sessionDays.add(day)
                lastSessionDay = day
            }
        }

        // Pass 2 (fallback): still short -- the requested day count matters more than the
        // rest-gap spacing once the preferred weekdays make strict spacing infeasible.
        if (sessionDays.size < totalSessions && minRestBetweenSessions) {
            for (day in 1..30) {
                if (sessionDays.size >= totalSessions) break
                if (day in sessionDays) continue
                if (((day - 1) % 7) + 1 in sortedWeekdays) sessionDays.add(day)
            }
            sessionDays.sort()
        }

        // Pass 3 (fallback): still short -- selectedWeekdays itself has fewer distinct weekdays than
        // the requested frequency needs per week; fill any remaining day of the month.
        if (sessionDays.size < totalSessions) {
            for (day in 1..30) {
                if (sessionDays.size >= totalSessions) break
                if (day !in sessionDays) sessionDays.add(day)
            }
            sessionDays.sort()
        }

        return sessionDays
    }

    fun templateFor(sessionIndex: Int): String = TEMPLATE_ORDER[sessionIndex % TEMPLATE_ORDER.size]

    /** Layer 6 "phase" + `override_konservatif`. */
    fun phaseFor(day: Int, mode: ProgressionMode): Phase {
        val adaptasiEnd = if (mode == ProgressionMode.CONSERVATIVE) 21 else 14
        val progresiEnd = if (mode == ProgressionMode.CONSERVATIVE) 28 else 28
        return when {
            day <= adaptasiEnd -> Phase("adaptasi", setCount = 2, rpeRange = 4..5)
            day <= progresiEnd -> Phase("progression", setCount = 3, rpeRange = 6..7)
            else -> Phase("buffer", setCount = 2, rpeRange = 4..5)
        }
    }

    /** WARM-01: cardio pool entry (or a no-equipment placeholder if none available), 3-5 minutes. */
    fun generalWarmup(pool: Map<MovementPattern, List<Exercise>>): CardioBlock {
        val cardio = pool[MovementPattern.CARDIO]?.firstOrNull()
        return CardioBlock(type = cardio?.name ?: "Jalan di tempat", durationMinutes = 4, intensity = "sangat ringan")
    }

    /** COOL-01: mandatory for HEALTH_HEART/HEALTH_BLOOD_PRESSURE -- caller must never truncate this for those flags. */
    fun heartRateCooldown(): CardioBlock = CardioBlock(type = "Jalan pelan", durationMinutes = 3, intensity = "sangat ringan")

    /** WARM-02: one lightest-regression set per pattern in this session, teaching-rep dose. */
    fun specificWarmup(sessionPatterns: List<MovementPattern>, pool: Map<MovementPattern, List<Exercise>>): List<PlannedExercise> =
        sessionPatterns.mapNotNull { pattern ->
            pool[pattern]?.firstOrNull()?.let { exercise ->
                PlannedExercise(exercise.id, sets = 1, repRangeOrDuration = "8-10 rep", rpeTargetMin = 3, rpeTargetMax = 4, reasonRuleIds = listOf("WARM-02"))
            }
        }

    /** COOL-02: 2-3 stretches whose `relatedMovementPatterns` overlaps this session's patterns, deduped against `mandatoryCorrective` (COOL-03). */
    fun cooldownStretch(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        mandatoryCorrective: List<Exercise>,
    ): List<PlannedExercise> {
        val korektifIds = mandatoryCorrective.map { it.id }.toSet()
        return pool[MovementPattern.STRETCH].orEmpty()
            .filter { it.id !in korektifIds && it.relatedMovementPatterns?.any { p -> p in sessionPatterns } == true }
            .take(3)
            .map { PlannedExercise(it.id, sets = 1, repRangeOrDuration = "20-30 detik", rpeTargetMin = 2, rpeTargetMax = 3, reasonRuleIds = listOf("COOL-02")) }
    }

    fun toWarmupBlock(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        mandatoryCorrective: List<Exercise>,
    ): WarmupBlock = WarmupBlock(
        general = generalWarmup(pool),
        specific = specificWarmup(sessionPatterns, pool),
        corrective = mandatoryCorrective.map {
            PlannedExercise(it.id, sets = 2, repRangeOrDuration = "10 rep", rpeTargetMin = 3, rpeTargetMax = 4, reasonRuleIds = listOf("SAFE-09", "WARM-03"))
        },
    )

    fun toCooldownBlock(
        sessionPatterns: List<MovementPattern>,
        pool: Map<MovementPattern, List<Exercise>>,
        mandatoryCorrective: List<Exercise>,
    ): CooldownBlock = CooldownBlock(
        heartRateCooldown = heartRateCooldown(),
        stretch = cooldownStretch(sessionPatterns, pool, mandatoryCorrective),
    )
}
