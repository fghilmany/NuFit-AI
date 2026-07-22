package com.fghilmany.nufitai.domain.onboarding.entity

import kotlin.time.Instant

data class BodyMeasurement(
    val id: String,
    val recordedAt: Instant,
    val heightCm: Double?,
    val weightKg: Double?,
) {
    val bmi: Double?
        get() {
            val height = heightCm
            val weight = weightKg
            if (height == null || weight == null || height <= 0.0) return null
            val heightMeters = height / 100.0
            return weight / (heightMeters * heightMeters)
        }
}
