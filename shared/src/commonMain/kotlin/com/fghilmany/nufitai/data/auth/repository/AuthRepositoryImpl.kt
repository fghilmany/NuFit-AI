package com.fghilmany.nufitai.data.auth.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.data.auth.datasource.AuthApiException
import com.fghilmany.nufitai.data.auth.datasource.AuthLocalDataSource
import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import kotlinx.coroutines.CancellationException

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
) : AuthRepository {
    private val logger = AppLogger.tagged("AuthRepository")

    override suspend fun signIn(
        provider: AuthProvider,
        idToken: String,
        displayName: String?,
        profilePictureUrl: String?,
    ): AppResult<AuthSession> {
        logger.i { "signIn starting for provider=$provider" }
        return try {
            val response = remote.signInWithIdToken(provider.name.lowercase(), idToken)
            val session = response.toEntity().copy(displayName = displayName, profilePictureUrl = profilePictureUrl)
            local.saveSession(session)
            logger.i { "signIn succeeded, userId=${session.userId}" }
            AppResult.Success(session)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthApiException) {
            logger.e(e) { "signIn failed -- HTTP ${e.statusCode}" }
            when (e.statusCode) {
                401 -> AppResult.Error(Failure.Unauthorized())
                422 -> AppResult.Error(Failure.Validation("Sign-in validation failed"))
                else -> AppResult.Error(Failure.Network("Auth backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "signIn failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }

    override suspend fun getCurrentSession(): AuthSession? = local.getSession()

    override suspend fun refreshSessionIfNeeded(): AppResult<AuthSession?> {
        val session = local.getSession() ?: return AppResult.Success(null)
        if (session.expiresAt > currentInstant()) return AppResult.Success(session)
        logger.i { "Refreshing expired session for userId=${session.userId}" }
        return try {
            val response = remote.refreshToken(session.refreshToken)
            // toEntity() never carries displayName/profilePictureUrl (Supabase's refresh
            // response doesn't echo them back) -- without this, every token refresh would
            // silently wipe the fields set at signIn.
            val refreshed = response.toEntity().copy(displayName = session.displayName, profilePictureUrl = session.profilePictureUrl)
            local.saveSession(refreshed)
            logger.i { "Session refresh succeeded" }
            AppResult.Success(refreshed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthApiException) {
            logger.e(e) { "Session refresh failed -- HTTP ${e.statusCode}" }
            if (e.statusCode == 401) {
                local.clearSession()
                AppResult.Error(Failure.Unauthorized())
            } else {
                AppResult.Error(Failure.Network("Auth backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "Session refresh failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        logger.i { "signOut" }
        local.clearSession()
        return AppResult.Success(Unit)
    }

    override suspend fun debugSetSession(session: AuthSession) {
        local.saveSession(session)
    }
}
