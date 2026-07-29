package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.profilesettings.datasource.AccountDeletionRemoteDataSource
import com.fghilmany.nufitai.data.profilesettings.datasource.ProfileSettingsLocalDataSource
import com.fghilmany.nufitai.data.profilesettings.repository.ProfileSettingsRepositoryImpl
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.AccountViewModel
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.MedicalDisclaimerViewModel
import com.fghilmany.nufitai.usecase.profilesettings.DeleteAccount
import com.fghilmany.nufitai.usecase.profilesettings.GetProfileSettingsSummary
import com.fghilmany.nufitai.usecase.profilesettings.GetWeightHistorySummary
import com.fghilmany.nufitai.usecase.profilesettings.HasSeenMedicalDisclaimer
import com.fghilmany.nufitai.usecase.profilesettings.MarkMedicalDisclaimerSeen
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. techspec profile-settings.md §7 -- `AccountViewModel`'s
 * registration moved here from `authModule` (feature-ownership cleanup that follows the
 * `presentation/account` -> `presentation/profilesettings` rename).
 */
val profileSettingsModule = module {
    single { ProfileSettingsLocalDataSource(settings = get()) }
    single { AccountDeletionRemoteDataSource(httpClient = get()) }
    single<ProfileSettingsRepository> { ProfileSettingsRepositoryImpl(local = get(), remote = get(), authRepository = get()) }

    // GetAllBodyMeasurements registered in onboardingModule (matches its usecase package) --
    // both load together in KoinInit.
    factory { GetWeightHistorySummary(getAllBodyMeasurements = get()) }
    factory { HasSeenMedicalDisclaimer(repository = get()) }
    factory { MarkMedicalDisclaimerSeen(repository = get()) }
    factory { DeleteAccount(repository = get(), signOut = get()) }
    factory {
        GetProfileSettingsSummary(
            getProfileBasics = get(),
            getLatestQuickAssessmentResult = get(),
            getLatestFullAssessmentResult = get(),
        )
    }

    // getCurrentUserProfile registered in authModule; getLanguage/setLanguage in languageModule --
    // all load together in KoinInit.
    viewModel {
        AccountViewModel(
            getCurrentTier = get(),
            signOut = get(),
            syncNow = get(),
            observeSyncStatus = get(),
            getWeightHistorySummary = get(),
            getProfileSettingsSummary = get(),
            getLatestBodyMeasurement = get(),
            saveBodyMeasurement = get(),
            deleteAccount = get(),
            getCurrentUserProfile = get(),
            getLanguage = get(),
            setLanguage = get(),
        )
    }
    viewModel { MedicalDisclaimerViewModel(markMedicalDisclaimerSeen = get()) }
}
