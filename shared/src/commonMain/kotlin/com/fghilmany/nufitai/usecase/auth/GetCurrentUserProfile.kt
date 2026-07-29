package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.domain.auth.repository.AuthRepository

data class CurrentUserProfile(val displayName: String?, val profilePictureUrl: String?)

/** Both null for guests, or when the session never captured them (e.g. debug session). */
class GetCurrentUserProfile(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): CurrentUserProfile {
        val session = authRepository.getCurrentSession()
        return CurrentUserProfile(displayName = session?.displayName, profilePictureUrl = session?.profilePictureUrl)
    }
}
