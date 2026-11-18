package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.onboarding.repository.OnboardingRepository

/**
 * Used by Splash routing (AC-1: new user -> PAR-Q/EntryChoice, AC-8: returning user -> Home).
 *
 * [OnboardingRepository.hasLocalProfile] alone only checks for a saved Quick Assessment result --
 * a user who chose "Login" from EntryChoice without ever completing Quick Assessment has a valid
 * session (real bug, 2026-11-18 dev report) but no such row, so Splash kept sending them back to
 * EntryChoice on every cold start despite already being logged in. Either signal is now enough.
 */
class GetLocalProfileStatus(
    private val repository: OnboardingRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<Boolean> {
        if (authRepository.getCurrentSession() != null) return AppResult.Success(true)
        return repository.hasLocalProfile()
    }
}
