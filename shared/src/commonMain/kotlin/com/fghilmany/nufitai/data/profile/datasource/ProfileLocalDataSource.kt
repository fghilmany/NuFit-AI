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

    suspend fun getChangedSince(updatedAfter: Long): List<UserProfileRow> = withContext(Dispatchers.Default) {
        database.userProfileQueries.selectChangedSince(updatedAfter).executeAsList()
    }

    suspend fun backfillUserId(userId: String) = withContext(Dispatchers.Default) {
        database.userProfileQueries.backfillUserId(userId)
    }

    suspend fun upsertFromRemote(row: UserProfileRow) = withContext(Dispatchers.Default) {
        database.userProfileQueries.upsertFromRemote(row.user_id, row.age, row.gender, row.updated_at)
    }
}
