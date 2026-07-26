package com.fghilmany.nufitai.data.profile.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.data.profile.datasource.ProfileLocalDataSource
import com.fghilmany.nufitai.db.User_profile as UserProfileRow
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.domain.profile.repository.ProfileRepository
import kotlin.time.Instant

class ProfileRepositoryImpl(
    private val local: ProfileLocalDataSource,
    private val authRepository: AuthRepository,
) : ProfileRepository {
    override suspend fun getProfileBasics(): AppResult<ProfileBasics?> = runCatchingDatabase {
        local.getProfileBasics()?.toEntity()
    }

    override suspend fun saveProfileBasics(age: Int, gender: Gender): AppResult<Unit> = runCatchingDatabase {
        val userId = authRepository.getCurrentSession()?.userId ?: "local"
        local.upsertProfileBasics(
            userId = userId,
            age = age.toLong(),
            gender = gender.name,
            updatedAt = currentInstant().toEpochMilliseconds(),
        )
    }

    private fun UserProfileRow.toEntity(): ProfileBasics = ProfileBasics(
        age = age.toInt(),
        gender = Gender.valueOf(gender),
        updatedAt = Instant.fromEpochMilliseconds(updated_at),
    )
}
