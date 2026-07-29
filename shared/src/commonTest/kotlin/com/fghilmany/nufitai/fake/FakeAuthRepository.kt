package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository

class FakeAuthRepository(
    private var session: AuthSession? = null,
    private var signInResult: AppResult<AuthSession>? = null,
) : AuthRepository {
    var signInCallCount = 0
        private set
    var signOutCallCount = 0
        private set

    override suspend fun signIn(
        provider: AuthProvider,
        idToken: String,
        displayName: String?,
        profilePictureUrl: String?,
    ): AppResult<AuthSession> {
        signInCallCount++
        return signInResult ?: AppResult.Error(Failure.Network("no fake result configured"))
    }

    override suspend fun getCurrentSession(): AuthSession? = session

    override suspend fun refreshSessionIfNeeded(): AppResult<AuthSession?> = AppResult.Success(session)

    override suspend fun signOut(): AppResult<Unit> {
        signOutCallCount++
        session = null
        return AppResult.Success(Unit)
    }

    override suspend fun debugSetSession(session: AuthSession) {
        this.session = session
    }

    fun setSignInResult(result: AppResult<AuthSession>) {
        signInResult = result
        if (result is AppResult.Success) session = result.data
    }
}
