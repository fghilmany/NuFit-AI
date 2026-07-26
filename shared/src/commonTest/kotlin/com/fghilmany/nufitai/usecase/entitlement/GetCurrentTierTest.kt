package com.fghilmany.nufitai.usecase.entitlement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetCurrentTierTest {

    @Test
    fun `given guest flags when invoked then returns GUEST`() = runTest {
        val getCurrentTier = GetCurrentTier(FakeEntitlementRepository(tier = Tier.GUEST))
        assertEquals(AppResult.Success(Tier.GUEST), getCurrentTier())
    }

    @Test
    fun `given logged in non subscriber when invoked then returns LOGGED_IN`() = runTest {
        val getCurrentTier = GetCurrentTier(FakeEntitlementRepository(tier = Tier.LOGGED_IN))
        assertEquals(AppResult.Success(Tier.LOGGED_IN), getCurrentTier())
    }

    @Test
    fun `given logged in and subscribed when invoked then returns SUBSCRIBED`() = runTest {
        val getCurrentTier = GetCurrentTier(FakeEntitlementRepository(tier = Tier.SUBSCRIBED))
        assertEquals(AppResult.Success(Tier.SUBSCRIBED), getCurrentTier())
    }
}
