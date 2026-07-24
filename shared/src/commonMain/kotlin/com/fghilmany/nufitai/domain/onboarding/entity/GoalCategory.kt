package com.fghilmany.nufitai.domain.onboarding.entity

enum class GoalCategory {
    FAT_LOSS,
    MUSCLE_GAIN,
    GENERAL_HEALTH,
    STRENGTH,

    /** Only reachable via Full Assessment's richer goal picker -- Quick Assessment's 4-goal set never produces it. */
    ENDURANCE,
}
