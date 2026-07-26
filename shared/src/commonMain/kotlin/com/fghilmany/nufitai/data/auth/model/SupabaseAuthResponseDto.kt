package com.fghilmany.nufitai.data.auth.model

import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds

@Serializable
data class SupabaseUserDto(val id: String)

@Serializable
data class SupabaseAuthResponseDto(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long,
    val user: SupabaseUserDto,
) {
    fun toEntity(): AuthSession = AuthSession(
        userId = user.id,
        accessToken = access_token,
        refreshToken = refresh_token,
        expiresAt = currentInstant() + expires_in.seconds,
    )
}

@Serializable
data class SignInWithIdTokenRequestDto(val provider: String, val id_token: String)

@Serializable
data class RefreshTokenRequestDto(val refresh_token: String)
