package com.fghilmany.nufitai.data.profile.datasource

import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.db.User_profile as UserProfileRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileLocalDataSource(private val database: NuFitDatabase) {
    suspend fun getProfileBasics(): UserProfileRow? = withContext(Dispatchers.Default) {
        database.userProfileQueries.getProfileBasics().executeAsOneOrNull()
    }

    suspend fun upsertProfileBasics(userId: String, age: Long, gender: String, updatedAt: Long) =
        withContext(Dispatchers.Default) {
            database.userProfileQueries.upsertProfileBasics(userId, age, gender, updatedAt)
        }
}
