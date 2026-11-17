package com.fghilmany.nufitai.data.onboarding.sync

import com.fghilmany.nufitai.data.onboarding.datasource.OnboardingLocalDataSource
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.db.Quick_assessment_result as QuickAssessmentResultRow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.time.Instant

/** Mirrors FullAssessmentSyncParticipant's worked-example pattern. */
class QuickAssessmentSyncParticipant(
    private val local: OnboardingLocalDataSource,
) : SyncParticipant {
    override val tableName: String = "quick_assessment_result"

    override suspend fun getChangedSince(since: Instant): List<JsonObject> =
        local.getQuickAssessmentChangedSince(since.toEpochMilliseconds()).map { it.toJson() }

    override suspend fun upsertRemoteRows(rows: List<JsonObject>) {
        local.upsertQuickAssessmentsFromRemote(rows.map { it.toRow() })
    }

    override suspend fun backfillUserId(userId: String) {
        local.backfillQuickAssessmentUserId(userId)
    }

    private fun QuickAssessmentResultRow.toJson(): JsonObject = buildJsonObject {
        put("id", JsonPrimitive(id))
        put("answered_at", JsonPrimitive(answered_at))
        put("experience", JsonPrimitive(experience))
        put("goal", JsonPrimitive(goal))
        put("equipment", JsonPrimitive(equipment))
        put("frequency", JsonPrimitive(frequency))
        put("split_preference", JsonPrimitive(split_preference))
        put("resolved_split", JsonPrimitive(resolved_split))
        put("level", JsonPrimitive(level))
        put("template_id", JsonPrimitive(template_id))
        put("split_educational_note", JsonPrimitive(split_educational_note))
        put("user_id", JsonPrimitive(user_id))
        put("updated_at", JsonPrimitive(updated_at))
    }

    private fun JsonObject.toRow(): QuickAssessmentResultRow = QuickAssessmentResultRow(
        id = getValue("id").jsonPrimitive.content,
        answered_at = getValue("answered_at").jsonPrimitive.long,
        experience = getValue("experience").jsonPrimitive.content,
        goal = getValue("goal").jsonPrimitive.content,
        equipment = getValue("equipment").jsonPrimitive.content,
        frequency = getValue("frequency").jsonPrimitive.content,
        split_preference = getValue("split_preference").jsonPrimitive.content,
        resolved_split = getValue("resolved_split").jsonPrimitive.content,
        level = getValue("level").jsonPrimitive.content,
        template_id = getValue("template_id").jsonPrimitive.content,
        split_educational_note = get("split_educational_note")?.jsonPrimitive?.contentOrNull,
        user_id = get("user_id")?.jsonPrimitive?.contentOrNull,
        updated_at = getValue("updated_at").jsonPrimitive.long,
    )
}
