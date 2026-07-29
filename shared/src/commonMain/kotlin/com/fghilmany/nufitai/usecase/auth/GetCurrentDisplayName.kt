package com.fghilmany.nufitai.usecase.auth

import com.fghilmany.nufitai.domain.auth.repository.AuthRepository

/** Null for guests, or when the session's display name wasn't captured (e.g. debug session pre-dating this field). */
class GetCurrentDisplayName(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): String? = authRepository.getCurrentSession()?.displayName
}
