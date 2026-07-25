package com.fghilmany.nufitai.domain.onboarding.entity

/** Exact selectable sessions/week (was a coarse 2-3x/4-5x range before). */
enum class FrequencyBucket(val sessionsPerWeek: Int) {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
}
