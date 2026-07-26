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
import kotlin.time.Instant

private val session = AuthSession(userId = "u1", accessToken = "a", refreshToken = "r", expiresAt = Instant.fromEpochMilliseconds(0))

class SignInTest {

    @Test
    fun `given successful sign in when invoked then sets logged in and triggers sync`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Success(session)) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository()
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertEquals(AppResult.Success(session), result)
        assertEquals(Tier.LOGGED_IN, entitlementRepository.tier)
        assertEquals(1, syncRepository.syncNowCallCount)
    }

    @Test
    fun `given failed sign in when invoked then does not set logged in or sync`() = runTest {
        val authRepository = FakeAuthRepository().apply { setSignInResult(AppResult.Error(Failure.Network("down"))) }
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.GUEST)
        val syncRepository = FakeSyncRepository()
        val signIn = SignIn(authRepository, entitlementRepository, syncRepository)

        val result = signIn(AuthProvider.GOOGLE, "id-token")

        assertEquals(AppResult.Error(Failure.Network("down")), result)
        assertEquals(Tier.GUEST, entitlementRepository.tier)
        assertEquals(0, syncRepository.syncNowCallCount)
    }
}
