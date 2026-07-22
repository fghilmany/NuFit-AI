package com.fghilmany.nufitai.domain.onboarding.entity

import kotlin.time.Instant

data class QuickAssessmentResult(
    val id: String,
    val answeredAt: Instant,
    val input: QuickAssessmentAnswer,
    val level: Level,
    val resolvedSplit: ResolvedSplit,
    val splitEducationalNote: String?,
    val templateId: String,
)
