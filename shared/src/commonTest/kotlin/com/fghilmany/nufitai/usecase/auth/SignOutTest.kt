package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.fake.FakeAuthRepository
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import com.fghilmany.nufitai.fake.FakeSyncRepository
import com.fghilmany.nufitai.core.error.Failure
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SignOutTest {

    @Test
    fun `given logged in user when sign out then syncs, clears session, reverts to guest, wipes local data`() = runTest {
        val authRepository = FakeAuthRepository()
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.LOGGED_IN)
        val syncRepository = FakeSyncRepository()
        val signOut = SignOut(authRepository, entitlementRepository, syncRepository)

        val result = signOut()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(1, authRepository.signOutCallCount)
        assertEquals(Tier.GUEST, entitlementRepository.tier)
        assertEquals(1, syncRepository.syncNowCallCount)
        assertEquals(1, syncRepository.clearAllLocalDataCallCount)
    }

    @Test
    fun `given sync fails when sign out then does not sign out or wipe data`() = runTest {
        val authRepository = FakeAuthRepository()
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.LOGGED_IN)
        val syncRepository = FakeSyncRepository().apply { syncNowResult = AppResult.Error(Failure.Network("offline")) }
        val signOut = SignOut(authRepository, entitlementRepository, syncRepository)

        val result = signOut()

        assertEquals(AppResult.Error(Failure.Network("offline")), result)
        assertEquals(0, authRepository.signOutCallCount)
        assertEquals(0, syncRepository.clearAllLocalDataCallCount)
    }

    @Test
    fun `given sync fails when sign out forced then signs out and wipes data anyway`() = runTest {
        val authRepository = FakeAuthRepository()
        val entitlementRepository = FakeEntitlementRepository(tier = Tier.LOGGED_IN)
        val syncRepository = FakeSyncRepository().apply { syncNowResult = AppResult.Error(Failure.Network("offline")) }
        val signOut = SignOut(authRepository, entitlementRepository, syncRepository)

        val result = signOut(force = true)

        assertEquals(AppResult.Success(Unit), result)
        assertEquals(0, syncRepository.syncNowCallCount)
        assertEquals(1, authRepository.signOutCallCount)
        assertEquals(1, syncRepository.clearAllLocalDataCallCount)
    }
}
