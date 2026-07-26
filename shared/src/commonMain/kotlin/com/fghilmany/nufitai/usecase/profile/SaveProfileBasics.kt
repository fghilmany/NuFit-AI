package com.fghilmany.nufitai.usecase.profile

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.profile.repository.ProfileRepository

class SaveProfileBasics(private val repository: ProfileRepository) {
    suspend operator fun invoke(age: Int, gender: Gender): AppResult<Unit> = repository.saveProfileBasics(age, gender)
}
