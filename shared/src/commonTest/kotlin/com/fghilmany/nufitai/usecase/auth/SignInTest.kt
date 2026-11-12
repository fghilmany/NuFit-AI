package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.auth.entity.AuthProvider
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.fake.FakeAuthRepository
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import com.fghilmany.nufitai.fake.FakeSyncRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

private val existingUserSession = AuthSession(
    userId = "u1", accessToken = "a", refreshToken = "r", expiresAt = Instant.fromEpochMilliseconds(0), isNewUser = false,
)
private val newUserSession = existingUserSession.copy(isNewUser = true)

class SignInTest {

    @Test
    fun `given a new account when invoked then signs in and syncs without checking for local data conflicts`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Success(newUserSession)) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository().apply { hasLocalUserDataResult = AppResult.Success(true) }
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertEquals(AppResult.Success(SignInOutcome.SignedIn(newUserSession)), result)
        assertEquals(Tier.LOGGED_IN, entitlementRepository.tier)
        assertEquals(1, syncRepository.syncNowCallCount)
    }

    @Test
    fun `given an existing account with no local data when invoked then signs in and syncs normally`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Success(existingUserSession)) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository().apply { hasLocalUserDataResult = AppResult.Success(false) }
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertEquals(AppResult.Success(SignInOutcome.SignedIn(existingUserSession)), result)
        assertEquals(1, syncRepository.syncNowCallCount)
    }

    @Test
    fun `given an existing account with pre-existing local guest data when invoked then returns a conflict without syncing`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Success(existingUserSession)) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository().apply { hasLocalUserDataResult = AppResult.Success(true) }
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertEquals(AppResult.Success(SignInOutcome.LocalDataConflict(existingUserSession)), result)
        assertEquals(Tier.LOGGED_IN, entitlementRepository.tier)
        assertEquals(0, syncRepository.syncNowCallCount)
    }

    @Test
    fun `given failed sign in when invoked then does not set logged in or sync`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Error(Failure.Network("down"))) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository()
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertIs<AppResult.Error>(result)
        assertEquals(Tier.GUEST, entitlementRepository.tier)
        assertEquals(0, syncRepository.syncNowCallCount)
    }
}
