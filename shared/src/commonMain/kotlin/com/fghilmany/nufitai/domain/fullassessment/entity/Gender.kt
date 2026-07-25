package com.fghilmany.nufitai.domain.fullassessment.entity

/**
 * Manual input only (Keputusan #11) -- never taken from a Google account. No "Lengkapi Profil"
 * screen exists yet (09-profile-settings.md, not built) -- Full Assessment collects it directly
 * for now; that screen will reuse this type when it ships, not duplicate it.
 */
enum class Gender { MALE, FEMALE }
