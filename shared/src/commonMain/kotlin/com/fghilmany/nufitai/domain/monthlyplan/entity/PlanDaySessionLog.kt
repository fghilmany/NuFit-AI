package com.fghilmany.nufitai.domain.monthlyplan.entity

import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import kotlin.time.Instant

/** Session-level only -- full per-set logging is PT Mode's table (`05-pt-mode.md`), not built yet. */
data class PlanDaySessionLog(
    val id: String,
    val planDayId: String,
    val completedAt: Instant?,
    val skippedAt: Instant?,
    val rpeReported: Int?,
    val painReported: Set<BodyArea>?,
)
