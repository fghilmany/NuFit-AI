package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.fake.FakeAuthRepository
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CancelConflictingSignInTest {

    @Test
    fun `given invoked then signs out and reverts to guest without touching local data`() = runTest {
        val authRepository = FakeAuthRepository()
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.LOGGED_IN)
        val cancelConflictingSignIn = CancelConflictingSignIn(authRepository, entitlementRepository)

        val result = cancelConflictingSignIn()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, authRepository.signOutCallCount)
        assertEquals(Tier.GUEST, entitlementRepository.tier)
    }
}
