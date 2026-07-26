package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.fake.FakeAuthRepository
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignOutTest {

    @Test
    fun `given logged in user when sign out then clears session and reverts to guest`() = runTest {
        val authRepository = FakeAuthRepository()
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.LOGGED_IN)
        val signOut = SignOut(authRepository, entitlementRepository)

        val result = signOut()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, authRepository.signOutCallCount)
        assertEquals(Tier.GUEST, entitlementRepository.tier)
    }
}
