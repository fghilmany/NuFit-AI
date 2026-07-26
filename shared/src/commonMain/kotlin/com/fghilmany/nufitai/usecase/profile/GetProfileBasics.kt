package com.fghilmany.nufitai.usecase.profile

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.domain.profile.repository.ProfileRepository

class GetProfileBasics(private val repository: ProfileRepository) {
    suspend operator fun invoke(): AppResult<ProfileBasics?> = repository.getProfileBasics()
}
