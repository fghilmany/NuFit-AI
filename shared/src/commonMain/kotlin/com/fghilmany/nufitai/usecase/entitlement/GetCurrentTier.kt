package com.fghilmany.nufitai.usecase.entitlement

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository

class GetCurrentTier(private val repository: EntitlementRepository) {
    suspend operator fun invoke(): AppResult<Tier> = repository.getCurrentTier()
}
