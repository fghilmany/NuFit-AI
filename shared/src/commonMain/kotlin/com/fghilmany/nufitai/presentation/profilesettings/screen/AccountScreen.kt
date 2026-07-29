package com.fghilmany.nufitai.presentation.profilesettings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.entitlement.entity.Tier
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.profile.entity.ProfileBasics
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.presentation.assessmentdetail.component.shortLabel
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import com.fghilmany.nufitai.presentation.profilesettings.component.BodyDataEditDialog
import com.fghilmany.nufitai.presentation.profilesettings.component.DeleteAccountConfirmDialog
import com.fghilmany.nufitai.presentation.profilesettings.component.LanguageSelectDialog
import com.fghilmany.nufitai.presentation.profilesettings.component.WeightHistoryChart
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.AccountEvent
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.AccountState
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.AccountViewModel
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.BodyDataOverview
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.fullassessment_interview_gender_female
import nufitai.shared.generated.resources.fullassessment_interview_gender_male
import nufitai.shared.generated.resources.profilesettings_assessment_link_subtitle
import nufitai.shared.generated.resources.profilesettings_assessment_link_subtitle_empty
import nufitai.shared.generated.resources.profilesettings_assessment_link_title
import nufitai.shared.generated.resources.profilesettings_body_data_section_title
import nufitai.shared.generated.resources.profilesettings_complete_profile_placeholder
import nufitai.shared.generated.resources.profilesettings_delete_account_action
import nufitai.shared.generated.resources.profilesettings_edit_body_data_title
import nufitai.shared.generated.resources.profilesettings_empty_value
import nufitai.shared.generated.resources.profilesettings_guest_hero_benefit_backup
import nufitai.shared.generated.resources.profilesettings_guest_hero_benefit_personalize
import nufitai.shared.generated.resources.profilesettings_guest_hero_cta_google
import nufitai.shared.generated.resources.profilesettings_guest_hero_title
import nufitai.shared.generated.resources.profilesettings_health_connect_coming_soon
import nufitai.shared.generated.resources.profilesettings_health_connect_title
import nufitai.shared.generated.resources.profilesettings_height_label
import nufitai.shared.generated.resources.profilesettings_height_unit
import nufitai.shared.generated.resources.profilesettings_info_about_app
import nufitai.shared.generated.resources.profilesettings_info_medical_disclaimer
import nufitai.shared.generated.resources.profilesettings_info_privacy_policy
import nufitai.shared.generated.resources.profilesettings_info_section_title
import nufitai.shared.generated.resources.profilesettings_language_english
import nufitai.shared.generated.resources.profilesettings_language_indonesian
import nufitai.shared.generated.resources.profilesettings_language_row_title
import nufitai.shared.generated.resources.profilesettings_level_label
import nufitai.shared.generated.resources.profilesettings_level_unknown
import nufitai.shared.generated.resources.profilesettings_logout_action
import nufitai.shared.generated.resources.profilesettings_notification_content_description
import nufitai.shared.generated.resources.profilesettings_profile_age_gender_format
import nufitai.shared.generated.resources.profilesettings_profile_assessment_section_title
import nufitai.shared.generated.resources.profilesettings_sync_now_action
import nufitai.shared.generated.resources.profilesettings_sync_section_title
import nufitai.shared.generated.resources.profilesettings_sync_status_failed
import nufitai.shared.generated.resources.profilesettings_sync_status_idle
import nufitai.shared.generated.resources.profilesettings_sync_status_success
import nufitai.shared.generated.resources.profilesettings_sync_status_syncing
import nufitai.shared.generated.resources.profilesettings_tier_free
import nufitai.shared.generated.resources.profilesettings_tier_subscribed_expiry
import nufitai.shared.generated.resources.profilesettings_top_bar_title
import nufitai.shared.generated.resources.profilesettings_unnamed_user
import nufitai.shared.generated.resources.profilesettings_version_footer
import nufitai.shared.generated.resources.profilesettings_weight_history_delta_format
import nufitai.shared.generated.resources.profilesettings_weight_history_title
import nufitai.shared.generated.resources.profilesettings_weight_label
import nufitai.shared.generated.resources.profilesettings_weight_unit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.time.Instant

private const val APP_VERSION = "1.0.0"

/** P-15 (techspec profile-settings.md). */
@Composable
fun AccountScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMedicalDisclaimer: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    onNavigateToAssessmentDetail: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
    onHomeClick: () -> Unit,
    onPlansClick: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar()

            when (val current = state) {
                AccountState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                is AccountState.Content -> ContentColumn(
                    state = current,
                    onEvent = viewModel::onEvent,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToMedicalDisclaimer = onNavigateToMedicalDisclaimer,
                    onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                    onNavigateToAboutApp = onNavigateToAboutApp,
                    onNavigateToAssessmentDetail = onNavigateToAssessmentDetail,
                    onNavigateToCompleteProfile = onNavigateToCompleteProfile,
                )
                is AccountState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            }
        }

        // Tab-switch, not a pushed screen (issue reported after first build): NavGraph pops
        // this destination off the stack the same way Home<->MyPlans already does.
        BottomNavBar(
            activeTab = NavTab.AKUN,
            onNavigate = { tab ->
                when (tab) {
                    NavTab.HOME -> onHomeClick()
                    NavTab.RENCANA -> onPlansClick()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.profilesettings_top_bar_title), style = MaterialTheme.typography.titleLarge)
        // Decorative only -- mirrors HomeScreen's/MyPlansScreen's existing static bell icon; no
        // notification system exists yet (techspec §9 item 2).
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Notifications, contentDescription = stringResource(Res.string.profilesettings_notification_content_description))
        }
    }
}

@Composable
private fun ContentColumn(
    state: AccountState.Content,
    onEvent: (AccountEvent) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToMedicalDisclaimer: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    onNavigateToAssessmentDetail: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 96.dp), // clears BottomNavBar (mirrors HomeScreen's contentPadding)
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        if (state.isLoggedIn) {
            LoggedInHeroCard(
                displayName = state.displayName,
                profilePictureUrl = state.profilePictureUrl,
                tier = state.tier,
                subscriptionExpiresAt = state.subscriptionExpiresAt,
                syncStatus = state.syncStatus,
                onSyncNow = { onEvent(AccountEvent.SyncNow) },
                onLogout = { onEvent(AccountEvent.SignOut) },
            )
        } else {
            GuestHeroCard(onNavigateToLogin)
        }

        BodyDataSection(
            bodyData = state.bodyData,
            onEditClick = { onEvent(AccountEvent.RequestEditBodyData) },
        )

        ProfileAssessmentSection(
            isLoggedIn = state.isLoggedIn,
            profileBasics = state.profileBasics,
            level = state.bodyData.level,
            latestAssessmentAt = state.bodyData.latestAssessmentAt,
            onCompleteProfileClick = onNavigateToCompleteProfile,
            onAssessmentClick = onNavigateToAssessmentDetail,
        )

        SyncIntegrationSection()

        InformationSection(
            language = state.language,
            onLanguageClick = { onEvent(AccountEvent.RequestChangeLanguage) },
            onMedicalDisclaimerClick = onNavigateToMedicalDisclaimer,
            onPrivacyPolicyClick = onNavigateToPrivacyPolicy,
            onAboutAppClick = onNavigateToAboutApp,
        )

        if (state.isLoggedIn) {
            DangerZoneSection(onDeleteAccountClick = { onEvent(AccountEvent.RequestDeleteAccount) })
        } else {
            VersionFooter()
        }
    }

    if (state.bodyDataEditDialogOpen) {
        BodyDataEditDialog(
            initialHeightCm = state.bodyData.latestHeightCm,
            initialWeightKg = state.bodyData.latestWeightKg,
            onDismiss = { onEvent(AccountEvent.CancelEditBodyData) },
            onSave = { heightCm, weightKg -> onEvent(AccountEvent.SaveBodyData(heightCm, weightKg)) },
        )
    }

    DeleteAccountConfirmDialog(
        state = state.deleteAccountDialog,
        onConfirm = { onEvent(AccountEvent.ConfirmDeleteAccount) },
        onCancel = { onEvent(AccountEvent.CancelDeleteAccount) },
    )

    if (state.languageDialogOpen) {
        LanguageSelectDialog(
            current = state.language,
            onSelect = { language -> onEvent(AccountEvent.SelectLanguage(language)) },
            onDismiss = { onEvent(AccountEvent.CancelChangeLanguage) },
        )
    }
}

@Composable
private fun GuestHeroCard(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NuFitColors.PrimaryContainer, RoundedCornerShape(24.dp))
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(Res.string.profilesettings_guest_hero_title),
            style = MaterialTheme.typography.titleMedium,
            color = NuFitColors.OnPrimaryContainer,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "• " + stringResource(Res.string.profilesettings_guest_hero_benefit_backup),
                color = NuFitColors.OnPrimaryContainer,
            )
            Text(
                "• " + stringResource(Res.string.profilesettings_guest_hero_benefit_personalize),
                color = NuFitColors.OnPrimaryContainer,
            )
        }
        AppButton(text = stringResource(Res.string.profilesettings_guest_hero_cta_google), onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun LoggedInHeroCard(
    displayName: String?,
    profilePictureUrl: String?,
    tier: Tier,
    subscriptionExpiresAt: Instant?,
    syncStatus: SyncStatus?,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(profilePictureUrl, modifier = Modifier.size(56.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        displayName ?: stringResource(Res.string.profilesettings_unnamed_user),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    TierBadge(tier, subscriptionExpiresAt)
                }
            }
            if (syncStatus != null) {
                Text(syncStatus.label(), style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onSyncNow) { Text(stringResource(Res.string.profilesettings_sync_now_action)) }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.profilesettings_logout_action))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(profilePictureUrl: String?, modifier: Modifier = Modifier) {
    if (profilePictureUrl == null) {
        Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Person, contentDescription = null)
        }
    } else {
        AsyncImage(
            model = profilePictureUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape).clip(CircleShape),
        )
    }
}

/** [subscriptionExpiresAt] is a dummy placeholder (§9 -- no billing integration yet). */
@Composable
private fun TierBadge(tier: Tier, subscriptionExpiresAt: Instant?) {
    val label = if (tier == Tier.SUBSCRIBED && subscriptionExpiresAt != null) {
        stringResource(Res.string.profilesettings_tier_subscribed_expiry, subscriptionExpiresAt.formatDate())
    } else {
        stringResource(Res.string.profilesettings_tier_free)
    }
    AppCard(
        backgroundColor = if (tier == Tier.SUBSCRIBED) NuFitColors.TertiaryContainer else NuFitColors.SurfaceContainerHigh,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (tier == Tier.SUBSCRIBED) NuFitColors.OnTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SyncStatus.label(): String = when (this) {
    SyncStatus.Idle -> stringResource(Res.string.profilesettings_sync_status_idle)
    SyncStatus.Syncing -> stringResource(Res.string.profilesettings_sync_status_syncing)
    is SyncStatus.Success -> {
        val local = lastSyncedAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
        stringResource(Res.string.profilesettings_sync_status_success, time)
    }
    is SyncStatus.Failed -> stringResource(Res.string.profilesettings_sync_status_failed)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 8.dp))
}

@Composable
private fun BodyDataSection(bodyData: BodyDataOverview, onEditClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle(stringResource(Res.string.profilesettings_body_data_section_title))
            // Adds a new weight-history row directly (issue reported after first build) --
            // previously only reachable by tapping a measurement card, not discoverable.
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(Res.string.profilesettings_edit_body_data_title))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            MeasurementCard(
                label = stringResource(Res.string.profilesettings_height_label),
                value = bodyData.latestHeightCm,
                unit = stringResource(Res.string.profilesettings_height_unit),
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
            )
            MeasurementCard(
                label = stringResource(Res.string.profilesettings_weight_label),
                value = bodyData.latestWeightKg,
                unit = stringResource(Res.string.profilesettings_weight_unit),
                onClick = onEditClick,
                modifier = Modifier.weight(1f),
            )
        }

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.profilesettings_weight_history_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    bodyData.weightHistory.deltaKgVsThirtyDaysAgo?.let { delta ->
                        val sign = if (delta > 0) "+" else ""
                        val rounded = (delta * 10).roundToInt() / 10.0
                        AppCard(backgroundColor = NuFitColors.TertiaryContainer, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                            Text(
                                stringResource(Res.string.profilesettings_weight_history_delta_format, "$sign$rounded"),
                                style = MaterialTheme.typography.labelSmall,
                                color = NuFitColors.OnTertiaryContainer,
                            )
                        }
                    }
                }
                WeightHistoryChart(points = bodyData.weightHistory.points, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun MeasurementCard(label: String, value: Double?, unit: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.clickable(onClick = onClick)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    value?.let { formatMeasurement(it) } ?: stringResource(Res.string.profilesettings_empty_value),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (value != null) Text(unit, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

private fun formatMeasurement(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toInt().toDouble()) rounded.toInt().toString() else rounded.toString()
}

@Composable
private fun ProfileAssessmentSection(
    isLoggedIn: Boolean,
    profileBasics: ProfileBasics?,
    level: Level?,
    latestAssessmentAt: Instant?,
    onCompleteProfileClick: () -> Unit,
    onAssessmentClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.profilesettings_profile_assessment_section_title), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(Res.string.profilesettings_level_label, level?.shortLabel() ?: stringResource(Res.string.profilesettings_level_unknown)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            Column {
                if (isLoggedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = profileBasics == null, onClick = onCompleteProfileClick).padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarCircle()
                        Text(
                            profileBasics?.let {
                                stringResource(Res.string.profilesettings_profile_age_gender_format, it.age, it.gender.label())
                            } ?: stringResource(Res.string.profilesettings_complete_profile_placeholder),
                            modifier = Modifier.weight(1f),
                        )
                        if (profileBasics == null) Icon(Icons.Filled.ChevronRight, contentDescription = null)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onAssessmentClick).padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AvatarCircle()
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.profilesettings_assessment_link_title), style = MaterialTheme.typography.titleSmall)
                        Text(
                            latestAssessmentAt?.let { stringResource(Res.string.profilesettings_assessment_link_subtitle, it.formatDate()) }
                                ?: stringResource(Res.string.profilesettings_assessment_link_subtitle_empty),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun Gender.label(): String = when (this) {
    Gender.MALE -> stringResource(Res.string.fullassessment_interview_gender_male)
    Gender.FEMALE -> stringResource(Res.string.fullassessment_interview_gender_female)
}

private fun Instant.formatDate(): String {
    val local = toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.dayOfMonth.toString().padStart(2, '0')}/${local.monthNumber.toString().padStart(2, '0')}/${local.year}"
}

@Composable
private fun AvatarCircle() {
    Box(
        modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SyncIntegrationSection() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(stringResource(Res.string.profilesettings_sync_section_title))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Sync, contentDescription = null)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(Res.string.profilesettings_health_connect_title), style = MaterialTheme.typography.titleSmall)
                }
                // Non-functional -- deferred, techspec §9 item 1 (Health Connect isn't built yet).
                AppCard(backgroundColor = NuFitColors.SurfaceContainerHigh, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(stringResource(Res.string.profilesettings_health_connect_coming_soon), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun InformationSection(
    language: AppLanguage,
    onLanguageClick: () -> Unit,
    onMedicalDisclaimerClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAboutAppClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle(stringResource(Res.string.profilesettings_info_section_title))
        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
            Column {
                InfoRow(
                    stringResource(Res.string.profilesettings_language_row_title),
                    onLanguageClick,
                    value = stringResource(
                        if (language == AppLanguage.INDONESIAN) Res.string.profilesettings_language_indonesian else Res.string.profilesettings_language_english,
                    ),
                )
                InfoRow(stringResource(Res.string.profilesettings_info_medical_disclaimer), onMedicalDisclaimerClick)
                InfoRow(stringResource(Res.string.profilesettings_info_privacy_policy), onPrivacyPolicyClick)
                InfoRow(stringResource(Res.string.profilesettings_info_about_app), onAboutAppClick)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, onClick: () -> Unit, value: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (value != null) Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun DangerZoneSection(onDeleteAccountClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onDeleteAccountClick) {
            Text(stringResource(Res.string.profilesettings_delete_account_action), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            stringResource(Res.string.profilesettings_version_footer, APP_VERSION),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun VersionFooter() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp), contentAlignment = Alignment.Center) {
        Text(
            stringResource(Res.string.profilesettings_version_footer, APP_VERSION),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
