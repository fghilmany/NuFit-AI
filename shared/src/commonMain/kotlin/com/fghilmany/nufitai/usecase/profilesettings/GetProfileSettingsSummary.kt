package com.fghilmany.nufitai.usecase.profilesettings

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.usecase.fullassessment.GetLatestFullAssessmentResult
import com.fghilmany.nufitai.usecase.onboarding.GetLatestQuickAssessmentResult
import com.fghilmany.nufitai.usecase.profile.GetProfileBasics
import kotlin.time.Instant

/**
 * techspec profile-settings.md §6/§9 item 5 -- presentation-facing aggregate for P-15, mirrors
 * `GetAssessmentDetail`'s composed-not-persisted pattern. [profileBasics] is `null` for guests
 * (never created) and for logged-in users who haven't completed "Lengkapi Profil" yet --
 * distinguishing those two cases is the caller's job (via `GetCurrentTier`), not this usecase's.
 * [level] comes from the Quick Assessment only -- Full Assessment doesn't carry a `Level` field
 * of its own (it feeds the tahap-7b rule engine differently).
 */
data class ProfileSettingsSummary(
    val profileBasics: ProfileBasics?,
    val latestAssessmentAt: Instant?,
    val level: Level?,
)

class GetProfileSettingsSummary(
    private val getProfileBasics: GetProfileBasics,
    private val getLatestQuickAssessmentResult: GetLatestQuickAssessmentResult,
    private val getLatestFullAssessmentResult: GetLatestFullAssessmentResult,
) {
    suspend operator fun invoke(): AppResult<ProfileSettingsSummary> {
        val profileBasics = when (val result = getProfileBasics()) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
        }

        val quickAssessment = when (val result = getLatestQuickAssessmentResult()) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data
        }

        val fullAssessmentAt = when (val result = getLatestFullAssessmentResult()) {
            is AppResult.Error -> return result
            is AppResult.Success -> result.data?.completedAt
        }

        val latestAssessmentAt = listOfNotNull(quickAssessment?.answeredAt, fullAssessmentAt).maxOrNull()

        return AppResult.Success(
            ProfileSettingsSummary(profileBasics = profileBasics, latestAssessmentAt = latestAssessmentAt, level = quickAssessment?.level),
        )
    }
}
