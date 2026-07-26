package com.fghilmany.nufitai.domain.aicustomplan.entity

/** Compact AI-context DTO (techspec §5/§9 item 7) -- mirrors `10-chat-ai.md`'s context-injection precedent. */
data class WorkoutHistorySummary(
    val cycleCount: Int,
    val attendanceRate: Float,
    val averageRpe: Float?,
    val completedSessionCount: Int,
)
