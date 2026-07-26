package com.fghilmany.nufitai.domain.profile.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics

interface ProfileRepository {
    suspend fun getProfileBasics(): AppResult<ProfileBasics?>
    suspend fun saveProfileBasics(age: Int, gender: Gender): AppResult<Unit>
}
