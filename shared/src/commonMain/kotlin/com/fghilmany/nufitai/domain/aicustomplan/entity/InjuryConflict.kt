package com.fghilmany.nufitai.domain.aicustomplan.entity

/** AC-5/AC-14 (BLOCKING) -- an AI-selected exercise reintroduces an excluded flag/loaded body area. */
data class InjuryConflict(val dayNumber: Int, val exerciseId: String)
