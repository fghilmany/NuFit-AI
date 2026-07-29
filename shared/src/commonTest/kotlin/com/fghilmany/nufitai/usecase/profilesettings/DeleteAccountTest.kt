package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.auth.entity.AuthSession
import com.fghilmany.nufitai.fake.FakeAuthRepository
import com.fghilmany.nufitai.fake.FakeEntitlementRepository
import com.fghilmany.nufitai.fake.FakeProfileSettingsRepository
import com.fghilmany.nufitai.usecase.auth.SignOut
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.hours

class DeleteAccountTest {
    private fun session() = AuthSession(
        userId = "u1", accessToken = "token", refreshToken = "refresh", expiresAt = currentInstant() + 1.hours,
    )

    @Test
    fun `given the backend deletes successfully when invoked then it signs out locally too`() = runTest {
        val profileSettingsRepository = FakeProfileSettingsRepository().apply { deleteAccountResult = AppResult.Success(Unit) }
        val authRepository = FakeAuthRepository(session = session())
        val signOut = SignOut(authRepository = authRepository, entitlementRepository = FakeEntitlementRepository())
        val deleteAccount = DeleteAccount(repository = profileSettingsRepository, signOut = signOut)

        val result = deleteAccount()

        assertIs<AppResult.Success<*>>(result)
        assertEquals(1, authRepository.signOutCallCount)
    }

    @Test
    fun `given the backend call fails when invoked then it does not sign out and returns the failure`() = runTest {
        val profileSettingsRepository = FakeProfileSettingsRepository().apply {
            deleteAccountResult = AppResult.Error(Failure.Network("unreachable"))
        }
        val authRepository = FakeAuthRepository(session = session())
        val signOut = SignOut(authRepository = authRepository, entitlementRepository = FakeEntitlementRepository())
        val deleteAccount = DeleteAccount(repository = profileSettingsRepository, signOut = signOut)

        val result = deleteAccount()

        assertIs<AppResult.Error>(result)
        assertEquals(0, authRepository.signOutCallCount)
    }
}
