package com.fghilmany.nufitai.domain.monthlyplan.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseLevel
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MovementPattern
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

data class MonthlyPlan(
    val id: String,
    val startedAt: Instant,
    /** 1, 2, 3... each 30-day cycle. */
    val cycleNumber: Int,
    val source: PlanSource,
    val status: PlanStatus,
    val levelMeta: String,
    val goalMeta: GoalCategory,
    val smartGoalMeta: String?,
    /** Union of all active flags at generation time -- "kenapa saya dapat gerakan ini?". */
    val activeFlags: Set<ExerciseFlag>,
    val startingLevelPerPattern: Map<MovementPattern, ExerciseLevel>,
    val mode: ProgressionMode,
    /** [14,21] normal, [21,28] conservative (PROG-06). */
    val checkpointDays: List<Int>,
)
