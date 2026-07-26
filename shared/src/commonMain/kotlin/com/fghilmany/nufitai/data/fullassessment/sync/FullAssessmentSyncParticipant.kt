package com.fghilmany.nufitai.data.fullassessment.sync

import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

/**
 * TODO(dev): stub -- techspec §3 sync-scope decision (auth-sync.md, issue #83) scoped this pass
 * to MonthlyPlan only. Follow `MonthlyPlanSyncParticipant` as the worked example: add
 * `user_id`/`updated_at` columns + `selectChangedSince`/`selectById`/`upsertFromRemote`/
 * `backfillUserId` queries to `FullAssessmentResult.sq` (nest `full_assessment_flag_detail`
 * children in the payload, same as PlanDay would nest under MonthlyPlan), then implement this
 * adapter for real.
 */
class FullAssessmentSyncParticipant : SyncParticipant {
    override val tableName: String = "full_assessment_result"
    override suspend fun getChangedSince(since: Instant): List<JsonObject> = emptyList()
    override suspend fun upsertRemoteRows(rows: List<JsonObject>) = Unit
    override suspend fun backfillUserId(userId: String) = Unit
}
