package com.fghilmany.nufitai.data.profile.sync

import com.fghilmany.nufitai.data.profile.datasource.ProfileLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.User_profile as UserProfileRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/**
 * "Lengkapi Profil" age/gender (CompleteProfileViewModel) had NO sync participant at all --
 * logout wipes local data (privacy, by design) and this row was never pushed to the cloud, so
 * every re-login forced the user back through "Lengkapi Profil". Mirrors
 * FullAssessmentSyncParticipant's worked-example pattern; one row per device (`getProfileBasics`'s
 * existing LIMIT 1 assumption).
 */
class ProfileBasicsSyncParticipant(
    private val local: ProfileLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "user_profile"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        rows.forEach { row ->
            val remoteUpdatedAt = row.getValue("updated_at").jsonPrimitive.long
            val existing = local.getProfileBasics()
            if (existing == null || existing.updated_at < remoteUpdatedAt) {
                local.upsertFromRemote(row.toRow())
            }
        }
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillUserId(userId)
    }

    private fun UserProfileRow.toJson(): JsonObject = buildJsonObject {
        put("user_id", JsonPrimitive(user_id))
        put("age", JsonPrimitive(age))
        put("gender", JsonPrimitive(gender))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): UserProfileRow = UserProfileRow(
        user_id = getValue("user_id").jsonPrimitive.content,
        age = getValue("age").jsonPrimitive.long,
        gender = getValue("gender").jsonPrimitive.content,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
