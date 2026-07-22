package com.fghilmany.nufitai.domain.onboarding.entity

import kotlin.time.Instant

data class ParQResult(
    val id: String,
    val answeredAt: Instant,
    val answers: List<ParQAnswer>,
    val flagsGenerated: Set<HealthFlag>,
    val requiresDoctorConsult: Boolean,
    val consultAcknowledgedAt: Instant?,
)
