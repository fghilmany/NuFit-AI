package com.fghilmany.nufitai.presentation.assessmentdetail.component

import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit

fun Level.shortLabel(): String = when (this) {
    Level.BEGINNER -> "Beginner"
    Level.INTERMEDIATE -> "Intermediate"
}

fun ResolvedSplit.shortLabel(): String = when (this) {
    ResolvedSplit.FULL_BODY -> "Full Body"
    ResolvedSplit.UPPER_LOWER -> "Upper/Lower"
}
