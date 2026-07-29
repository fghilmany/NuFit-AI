package com.fghilmany.nufitai.presentation.profilesettings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.domain.profilesettings.entity.WeightHistorySummary
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.usecase.auth.GetCurrentUserProfile
import com.fghilmany.nufitai.usecase.auth.SignOut
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.language.GetLanguage
import com.fghilmany.nufitai.usecase.language.SetLanguage
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import com.fghilmany.nufitai.usecase.onboarding.SaveBodyMeasurement
import com.fghilmany.nufitai.usecase.profilesettings.DeleteAccount
import com.fghilmany.nufitai.usecase.profilesettings.GetProfileSettingsSummary
import com.fghilmany.nufitai.usecase.profilesettings.GetWeightHistorySummary
import com.fghilmany.nufitai.usecase.sync.ObserveSyncStatus
import com.fghilmany.nufitai.usecase.sync.SyncNow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** P-15 (techspec profile-settings.md §6). */
data class BodyDataOverview(
    val weightHistory: WeightHistorySummary,
    val latestHeightCm: Double?,
    val latestWeightKg: Double?,
    val latestAssessmentAt: Instant?,
    val level: Level?,
)

sealed interface DeleteAccountDialogState {
    data object Hidden : DeleteAccountDialogState
    data object Confirming : DeleteAccountDialogState
    data object Deleting : DeleteAccountDialogState
    data class Failed(val message: String) : DeleteAccountDialogState
}

sealed interface AccountState {
    data object Loading : AccountState

    /**
     * [isLoggedIn] false = Guest (§9 item: Data Tubuh/assessment sections still show -- local-first,
     * not gated -- only the hero card, Sinkronisasi, and Danger Zone differ by tier).
     * [syncStatus] is null for Guest. [profileBasics] is only ever non-null when [isLoggedIn].
     * [tier]/[subscriptionExpiresAt]: [subscriptionExpiresAt] is a DUMMY placeholder (no billing
     * integration exists yet, per RevenueCat being a separate later feature) -- only meaningful
     * when [tier] is [Tier.SUBSCRIBED].
     */
    data class Content(
        val isLoggedIn: Boolean,
        val syncStatus: SyncStatus?,
        val bodyData: BodyDataOverview,
        val profileBasics: ProfileBasics?,
        val displayName: String?,
        val profilePictureUrl: String?,
        val tier: Tier,
        val subscriptionExpiresAt: Instant?,
        val language: AppLanguage,
        val bodyDataEditDialogOpen: Boolean = false,
        val languageDialogOpen: Boolean = false,
        val deleteAccountDialog: DeleteAccountDialogState = DeleteAccountDialogState.Hidden,
    ) : AccountState

    data class Error(val message: String) : AccountState
}

sealed interface AccountEvent {
    data object SyncNow : AccountEvent
    data object SignOut : AccountEvent
    data object RequestEditBodyData : AccountEvent
    data object CancelEditBodyData : AccountEvent
    data class SaveBodyData(val heightCm: Double?, val weightKg: Double?) : AccountEvent
    data object RequestDeleteAccount : AccountEvent
    data object CancelDeleteAccount : AccountEvent
    data object ConfirmDeleteAccount : AccountEvent
    data object RequestChangeLanguage : AccountEvent
    data object CancelChangeLanguage : AccountEvent
    data class SelectLanguage(val language: AppLanguage) : AccountEvent
}

private val DUMMY_SUBSCRIPTION_DURATION = 30.days

class AccountViewModel(
    private val getCurrentTier: GetCurrentTier,
    private val signOut: SignOut,
    private val syncNow: SyncNow,
    private val observeSyncStatus: ObserveSyncStatus,
    private val getWeightHistorySummary: GetWeightHistorySummary,
    private val getProfileSettingsSummary: GetProfileSettingsSummary,
    private val getLatestBodyMeasurement: GetLatestBodyMeasurement,
    private val saveBodyMeasurement: SaveBodyMeasurement,
    private val deleteAccount: DeleteAccount,
    private val getCurrentUserProfile: GetCurrentUserProfile,
    private val getLanguage: GetLanguage,
    private val setLanguage: SetLanguage,
) : ViewModel() {
    private val _state = MutableStateFlow<AccountState>(AccountState.Loading)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    init {
        resolve()
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            AccountEvent.SyncNow -> syncNowManually()
            AccountEvent.SignOut -> signOutNow()
            AccountEvent.RequestEditBodyData -> updateContent { it.copy(bodyDataEditDialogOpen = true) }
            AccountEvent.CancelEditBodyData -> updateContent { it.copy(bodyDataEditDialogOpen = false) }
            is AccountEvent.SaveBodyData -> saveBodyData(event.heightCm, event.weightKg)
            AccountEvent.RequestDeleteAccount -> updateContent { it.copy(deleteAccountDialog = DeleteAccountDialogState.Confirming) }
            AccountEvent.CancelDeleteAccount -> updateContent { it.copy(deleteAccountDialog = DeleteAccountDialogState.Hidden) }
            AccountEvent.ConfirmDeleteAccount -> confirmDeleteAccount()
            AccountEvent.RequestChangeLanguage -> updateContent { it.copy(languageDialogOpen = true) }
            AccountEvent.CancelChangeLanguage -> updateContent { it.copy(languageDialogOpen = false) }
            is AccountEvent.SelectLanguage -> selectLanguage(event.language)
        }
    }

    private fun updateContent(transform: (AccountState.Content) -> AccountState.Content) {
        _state.update { current -> if (current is AccountState.Content) transform(current) else current }
    }

    private fun syncNowManually() {
        viewModelScope.launch { syncNow() }
    }

    private fun signOutNow() {
        viewModelScope.launch {
            signOut()
            resolve()
        }
    }

    private fun selectLanguage(language: AppLanguage) {
        viewModelScope.launch {
            setLanguage(language)
            updateContent { it.copy(language = language, languageDialogOpen = false) }
        }
    }

    private fun saveBodyData(heightCm: Double?, weightKg: Double?) {
        viewModelScope.launch {
            when (saveBodyMeasurement(heightCm, weightKg)) {
                is AppResult.Success -> {
                    updateContent { it.copy(bodyDataEditDialogOpen = false) }
                    reloadBodyData()
                }
                is AppResult.Error -> updateContent { it.copy(bodyDataEditDialogOpen = false) }
            }
        }
    }

    private fun confirmDeleteAccount() {
        updateContent { it.copy(deleteAccountDialog = DeleteAccountDialogState.Deleting) }
        viewModelScope.launch {
            when (val result = deleteAccount()) {
                is AppResult.Success -> resolve()
                is AppResult.Error -> updateContent {
                    it.copy(deleteAccountDialog = DeleteAccountDialogState.Failed(result.failure.message))
                }
            }
        }
    }

    private suspend fun reloadBodyData() {
        val bodyData = loadBodyData() ?: return
        updateContent { it.copy(bodyData = bodyData) }
    }

    private suspend fun loadBodyData(): BodyDataOverview? {
        val weightHistory = when (val result = getWeightHistorySummary()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return null
        }
        // Deliberately separate from `weightHistory.points` (which drops height-only rows) --
        // the height/weight cards must reflect the true latest measurement of each, independently.
        val latestMeasurement = when (val result = getLatestBodyMeasurement()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return null
        }
        val profileSummary = when (val result = getProfileSettingsSummary()) {
            is AppResult.Success -> result.data
            is AppResult.Error -> return null
        }
        return BodyDataOverview(
            weightHistory = weightHistory,
            latestHeightCm = latestMeasurement?.heightCm,
            latestWeightKg = latestMeasurement?.weightKg,
            latestAssessmentAt = profileSummary.latestAssessmentAt,
            level = profileSummary.level,
        )
    }

    private fun resolve() {
        viewModelScope.launch {
            val bodyData = loadBodyData() ?: BodyDataOverview(
                weightHistory = WeightHistorySummary(points = emptyList(), deltaKgVsThirtyDaysAgo = null),
                latestHeightCm = null,
                latestWeightKg = null,
                latestAssessmentAt = null,
                level = null,
            )
            val language = when (val result = getLanguage()) {
                is AppResult.Success -> result.data
                is AppResult.Error -> AppLanguage.INDONESIAN
            }

            when (val tierResult = getCurrentTier()) {
                is AppResult.Error -> _state.value = AccountState.Error(tierResult.failure.message)
                is AppResult.Success -> when (tierResult.data) {
                    Tier.GUEST -> _state.value = AccountState.Content(
                        isLoggedIn = false, syncStatus = null, bodyData = bodyData, profileBasics = null,
                        displayName = null, profilePictureUrl = null, tier = Tier.GUEST, subscriptionExpiresAt = null,
                        language = language,
                    )
                    Tier.LOGGED_IN, Tier.SUBSCRIBED -> observeLoggedIn(bodyData, tierResult.data, language)
                }
            }
        }
    }

    private suspend fun observeLoggedIn(bodyData: BodyDataOverview, tier: Tier, language: AppLanguage) {
        val profileBasics = when (val result = getProfileSettingsSummary()) {
            is AppResult.Success -> result.data.profileBasics
            is AppResult.Error -> null
        }
        val userProfile = getCurrentUserProfile()
        // DUMMY -- no billing integration yet (RevenueCat is a separate later feature); this is
        // a placeholder expiry so the UI has something concrete to show for SUBSCRIBED.
        val subscriptionExpiresAt = if (tier == Tier.SUBSCRIBED) currentInstant() + DUMMY_SUBSCRIPTION_DURATION else null
        val statusFlow = observeSyncStatus()
        _state.value = AccountState.Content(
            isLoggedIn = true, syncStatus = statusFlow.value, bodyData = bodyData, profileBasics = profileBasics,
            displayName = userProfile.displayName, profilePictureUrl = userProfile.profilePictureUrl,
            tier = tier, subscriptionExpiresAt = subscriptionExpiresAt, language = language,
        )
        viewModelScope.launch {
            statusFlow.collect { status -> updateContent { it.copy(syncStatus = status) } }
        }
    }
}
