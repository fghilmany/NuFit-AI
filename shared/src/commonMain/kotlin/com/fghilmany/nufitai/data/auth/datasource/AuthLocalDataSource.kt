package com.fghilmany.nufitai.data.auth.datasource

import com.fghilmany.nufitai.core.security.SecureStorage
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class StoredSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochMillis: Long,
    val displayName: String? = null,
    val profilePictureUrl: String? = null,
)

/** Wraps [SecureStorage] (Keystore/Keychain-encrypted in production) and (de)serializes [AuthSession]. */
class AuthLocalDataSource(private val storage: SecureStorage) {
    private val json = Json

    fun getSession(): AuthSession? {
        val raw = storage.read() ?: return null
        val stored = json.decodeFromString(StoredSession.serializer(), raw)
        return AuthSession(
            userId = stored.userId,
            accessToken = stored.accessToken,
            refreshToken = stored.refreshToken,
            expiresAt = kotlin.time.Instant.fromEpochMilliseconds(stored.expiresAtEpochMillis),
            displayName = stored.displayName,
            profilePictureUrl = stored.profilePictureUrl,
        )
    }

    fun saveSession(session: AuthSession) {
        val stored = StoredSession(
            userId = session.userId,
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            expiresAtEpochMillis = session.expiresAt.toEpochMilliseconds(),
            displayName = session.displayName,
            profilePictureUrl = session.profilePictureUrl,
        )
        storage.save(json.encodeToString(StoredSession.serializer(), stored))
    }

    fun clearSession() {
        storage.clear()
    }
}
