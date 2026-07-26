package com.fghilmany.nufitai.data.auth.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
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
    override suspend fun signIn(provider: AuthProvider, idToken: String): AppResult<AuthSession> = try {
        val response = remote.signInWithIdToken(provider.name.lowercase(), idToken)
        val session = response.toEntity()
        local.saveSession(session)
        AppResult.Success(session)
    } catch (e: CancellationException) {
        throw e
    } catch (e: AuthApiException) {
        when (e.statusCode) {
            401 -> AppResult.Error(Failure.Unauthorized())
            422 -> AppResult.Error(Failure.Validation("Sign-in validation failed"))
            else -> AppResult.Error(Failure.Network("Auth backend error (${e.statusCode})"))
        }
    } catch (e: Exception) {
        AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
    }

    override suspend fun getCurrentSession(): AuthSession? = local.getSession()

    override suspend fun refreshSessionIfNeeded(): AppResult<AuthSession?> {
        val session = local.getSession() ?: return AppResult.Success(null)
        if (session.expiresAt > currentInstant()) return AppResult.Success(session)
        return try {
            val response = remote.refreshToken(session.refreshToken)
            val refreshed = response.toEntity()
            local.saveSession(refreshed)
            AppResult.Success(refreshed)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthApiException) {
            if (e.statusCode == 401) {
                local.clearSession()
                AppResult.Error(Failure.Unauthorized())
            } else {
                AppResult.Error(Failure.Network("Auth backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        local.clearSession()
        return AppResult.Success(Unit)
    }

    override suspend fun debugSetSession(session: AuthSession) {
        local.saveSession(session)
    }
}
