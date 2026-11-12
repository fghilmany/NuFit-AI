package com.fghilmany.nufitai.domain.auth.entity

import kotlin.time.Instant

data class AuthSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant,
    /** Best-effort, captured from the native sign-in provider at login time (never re-fetched). */
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
    /** Only meaningful right after [com.fghilmany.nufitai.usecase.auth.SignIn] -- never persisted
     * (defaults to false on every reload), since it only matters at the moment of signing in. */
    val isNewUser: Boolean = false,
)
