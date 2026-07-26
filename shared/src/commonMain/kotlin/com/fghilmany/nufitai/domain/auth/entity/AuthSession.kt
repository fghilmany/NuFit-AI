package com.fghilmany.nufitai.domain.auth.entity

import kotlin.time.Instant

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
)
