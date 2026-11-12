package com.fghilmany.nufitai.data.auth.model

import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Serializable
data class SupabaseUserDto(
    val id: String,
    /** ISO-8601, immutable since account creation. Compared against [last_sign_in_at] to tell a
     * brand-new account (this is its first-ever sign-in -- see [SupabaseAuthResponseDto.toEntity]). */
    val created_at: String? = null,
    val last_sign_in_at: String? = null,
)

@Serializable
data class SupabaseAuthResponseDto(
    val access_token: String,
    val refresh_token: String,
    val expires_in: Long,
    val user: SupabaseUserDto,
) {
    /** Within a few seconds of each other = the account's very first sign-in (Supabase stamps
     * both at account-creation time); anything further apart means this user has signed in
     * before. Tolerance (not exact string equality) absorbs sub-second processing drift between
     * the two timestamps even on a genuinely brand-new account. */
    private fun isNewUser(): Boolean {
        val createdAt = user.created_at?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
        val lastSignInAt = user.last_sign_in_at?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
        return (lastSignInAt - createdAt) < 5.seconds
    }

    fun toEntity(): AuthSession = AuthSession(
        userId = user.id,
        accessToken = access_token,
        refreshToken = refresh_token,
        expiresAt = currentInstant() + expires_in.seconds,
        isNewUser = isNewUser(),
    )
}

@Serializable
data class SignInWithIdTokenRequestDto(val provider: String, val id_token: String)

@Serializable
data class RefreshTokenRequestDto(val refresh_token: String)
