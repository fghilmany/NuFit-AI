package com.fghilmany.nufitai.usecase.onboarding

import com.fghilmany.nufitai.domain.onboarding.entity.Experience
import com.fghilmany.nufitai.domain.onboarding.entity.FrequencyBucket
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.QuickAssessmentAnswer
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.domain.onboarding.entity.SplitPreference

data class ResolvedQuickAssessment(
    val level: Level,
    val resolvedSplit: ResolvedSplit,
    val splitEducationalNote: String?,
    val templateId: String,
)

/**
 * Pure decision-table function (techspec section 5 "Business Rules"):
 * Level x Goal x Split x Frequency -> templateId.
 *
 * No capacity/movement-screening data exists at the Quick Assessment (Lokal) tier
 * -- level is derived from self-reported experience alone and capped conservatively
 * at Intermediate (Advanced requires Full Assessment validation, a different tier).
 */
class ResolveQuickAssessment() {
    operator fun invoke(input: QuickAssessmentAnswer): ResolvedQuickAssessment {
        val level = resolveLevel(input)
        val (resolvedSplit, note) = resolveSplit(input, level)
        val templateId = buildTemplateId(level, input.goal, resolvedSplit, input.frequency)

        return ResolvedQuickAssessment(
            level = level,
            resolvedSplit = resolvedSplit,
            splitEducationalNote = note,
            templateId = templateId,
        )
    }

    private fun resolveLevel(input: QuickAssessmentAnswer): Level =
        when (input.experience) {
            Experience.BELUM_PERNAH, Experience.KURANG_1_TAHUN -> Level.BEGINNER
            Experience.RUTIN_BERTAHUN -> Level.INTERMEDIATE
        }

    private fun resolveSplit(input: QuickAssessmentAnswer, level: Level): Pair<ResolvedSplit, String?> =
        when (input.splitPreference) {
            SplitPreference.FULL_BODY -> ResolvedSplit.FULL_BODY to null

            SplitPreference.UPPER_LOWER -> {
                val note = if (input.frequency == FrequencyBucket.TWO_TO_THREE) {
                    "Upper/Lower biasanya kurang ideal untuk frekuensi 2-3x/minggu -- volume per bagian tubuh jadi terlalu sedikit. Full Body akan memberi hasil lebih konsisten di frekuensi ini, tapi pilihanmu tetap kami pakai."
                } else {
                    null
                }
                ResolvedSplit.UPPER_LOWER to note
            }

            SplitPreference.TIDAK_TAHU -> {
                val resolved = if (level == Level.BEGINNER || input.frequency == FrequencyBucket.TWO_TO_THREE) {
                    ResolvedSplit.FULL_BODY
                } else {
                    ResolvedSplit.UPPER_LOWER
                }
                val note = if (resolved == ResolvedSplit.FULL_BODY) {
                    "Kami pilihkan Full Body karena ini paling efektif untuk levelmu dan frekuensi latihanmu saat ini."
                } else {
                    "Kami pilihkan Upper/Lower karena frekuensi latihanmu cukup untuk membagi fokus atas dan bawah tubuh."
                }
                resolved to note
            }
        }

    private fun buildTemplateId(
        level: Level,
        goal: GoalCategory,
        resolvedSplit: ResolvedSplit,
        frequency: FrequencyBucket,
    ): String = "${level.name}_${goal.name}_${resolvedSplit.name}_${frequency.name}"
}
