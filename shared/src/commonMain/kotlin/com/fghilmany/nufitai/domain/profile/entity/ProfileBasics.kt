package com.fghilmany.nufitai.domain.profile.entity

import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import kotlin.time.Instant

/**
 * "Lengkapi Profil" (Keputusan #11, techspec §5 auth-sync.md) -- age/gender captured once right
 * after first login, manual input only, decoupled from [com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult].
 * Reuses the existing [Gender] type rather than duplicating it (see that enum's own doc comment).
 */
data class ProfileBasics(val age: Int, val gender: Gender, val updatedAt: Instant)
