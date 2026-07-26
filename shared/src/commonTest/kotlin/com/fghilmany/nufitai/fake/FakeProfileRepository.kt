package com.fghilmany.nufitai.fake

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.domain.profile.repository.ProfileRepository

class FakeProfileRepository(private var stored: ProfileBasics? = null) : ProfileRepository {
    override suspend fun getProfileBasics(): AppResult<ProfileBasics?> = AppResult.Success(stored)

    override suspend fun saveProfileBasics(age: Int, gender: Gender): AppResult<Unit> {
        stored = ProfileBasics(age = age, gender = gender, updatedAt = currentInstant())
        return AppResult.Success(Unit)
    }
}
