package com.fghilmany.nufitai.usecase.monthlyplan.rules

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDaySessionLog
import com.fghilmany.nufitai.domain.monthlyplan.entity.ProgressionMode
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * issue #29 layer 7 -- PROG-01..06. Pure, no I/O.
 *
 * KNOWN LIMITATION: PROG-01's 3 criteria are specified per-pattern against rep/RPE actuals
 * logged per exercise. `PlanDaySessionLog` (this techspec's scope, §9 item 18) is intentionally
 * session-level only -- one `rpeReported`/`painReported` for the whole session, not per
 * exercise/pattern. [canLevelUp] therefore evaluates against session-level data as the closest
 * available approximation (session RPE <= phase target, no session-wide pain report) until
 * PT Mode (`05-pt-mode.md`) ships per-set logging with a `plan_day_id` FK.
 */
object ProgressionGating {

    /** PROG-06: checkpoint days shift when conservative mode is active. */
    fun checkpointDays(mode: ProgressionMode): List<Int> =
        if (mode == ProgressionMode.CONSERVATIVE) listOf(21, 28) else listOf(14, 21)

    /**
     * PROG-06: on day 14 in conservative mode, log only -- never gate.
     * PROG-01/02: needs the 2 most recent COMPLETED sessions that included this pattern.
     */
    fun canLevelUp(
        mode: ProgressionMode,
        checkpointDay: Int,
        recentSessionLogsForPattern: List<PlanDaySessionLog>, // most recent first, already filtered to this pattern's sessions
        rpeTargetMax: Int,
        painAreasForPattern: Set<BodyArea>,
    ): Boolean {
        if (mode == ProgressionMode.CONSERVATIVE && checkpointDay == 14) return false // PROG-06

        val lastTwo = recentSessionLogsForPattern.take(2)
        if (lastTwo.size < 2) return false
        return lastTwo.all { log ->
            log.completedAt != null &&
                (log.rpeReported == null || log.rpeReported <= rpeTargetMax) &&
                (log.painReported.orEmpty().intersect(painAreasForPattern)).isEmpty()
        }
    }

    /** PROG-03: repeated pain report for a pattern's area -> drop one level + trigger partial re-assessment. */
    fun shouldLevelDown(recentSessionLogsForPattern: List<PlanDaySessionLog>, painAreasForPattern: Set<BodyArea>): Boolean =
        recentSessionLogsForPattern.count { it.painReported.orEmpty().intersect(painAreasForPattern).isNotEmpty() } >= 2

    /** PROG-04: absence > 14 days -> freeze calendar, offer light re-assessment. */
    fun isAbsent(lastSessionAt: Instant?, now: Instant, thresholdDays: Int = 14): Boolean =
        lastSessionAt == null || (now - lastSessionAt) > thresholdDays.days
}
