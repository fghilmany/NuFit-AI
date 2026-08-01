package com.fghilmany.nufitai.di

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.fghilmany.nufitai.core.di.aiCustomPlanModule
import com.fghilmany.nufitai.core.di.assessmentDetailModule
import com.fghilmany.nufitai.core.di.chatAiModule
import com.fghilmany.nufitai.core.di.mealPlanModule
import com.fghilmany.nufitai.core.di.exerciseLibraryModule
import com.fghilmany.nufitai.core.di.fullAssessmentModule
import com.fghilmany.nufitai.core.di.languageModule
import com.fghilmany.nufitai.core.di.monthlyPlanModule
import com.fghilmany.nufitai.core.di.networkModule
import com.fghilmany.nufitai.core.di.onboardingModule
import com.fghilmany.nufitai.core.di.planManagementModule
import com.fghilmany.nufitai.core.di.profileModule
import com.fghilmany.nufitai.core.di.profileSettingsModule
import com.fghilmany.nufitai.core.di.ptModeModule
import com.fghilmany.nufitai.core.di.syncModule
import com.fghilmany.nufitai.core.security.SecureStorage
import com.fghilmany.nufitai.data.auth.datasource.AuthLocalDataSource
import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import com.fghilmany.nufitai.data.auth.repository.AuthRepositoryImpl
import com.fghilmany.nufitai.data.entitlement.datasource.EntitlementLocalDataSource
import com.fghilmany.nufitai.data.entitlement.repository.EntitlementRepositoryImpl
import com.fghilmany.nufitai.db.NuFitDatabase
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.entitlement.repository.EntitlementRepository
import com.fghilmany.nufitai.core.auth.AppleSignInLauncher
import com.fghilmany.nufitai.core.auth.GoogleSignInLauncher
import com.fghilmany.nufitai.presentation.auth.viewmodel.CompleteProfileViewModel
import com.fghilmany.nufitai.presentation.auth.viewmodel.LoginViewModel
import com.fghilmany.nufitai.usecase.auth.DebugSimulateSignIn
import com.fghilmany.nufitai.usecase.auth.GetCurrentDisplayName
import com.fghilmany.nufitai.usecase.auth.GetCurrentUserProfile
import com.fghilmany.nufitai.usecase.auth.SignIn
import com.fghilmany.nufitai.usecase.auth.SignOut
import com.fghilmany.nufitai.usecase.entitlement.GetCurrentTier
import com.fghilmany.nufitai.usecase.entitlement.SetDebugEntitlement
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.check.checkModules
import kotlin.test.AfterTest
import kotlin.test.Test

class DiVerificationTest {

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun verifyKoinConfiguration() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NuFitDatabase.Schema.create(driver)

        startKoin {
            modules(
                module { single { NuFitDatabase(driver = driver) } },
                // issue #81 -- SettingsFactory needs an Android Context this JVM host test doesn't
                // have; mirrors how databaseModule(driverFactory) is bypassed above with a direct
                // NuFitDatabase binding instead.
                module {
                    // Shared across this block and profileSettingsModule's ProfileSettingsLocalDataSource
                    // (mirrors how entitlementModule(settingsFactory) exposes Settings for real).
                    single<Settings> { MapSettings() }
                    single { EntitlementLocalDataSource(settings = get()) }
                    single<EntitlementRepository> { EntitlementRepositoryImpl(local = get()) }
                    factory { GetCurrentTier(repository = get()) }
                    factory { SetDebugEntitlement(repository = get()) }
                },
                networkModule,
                // issue #83 -- TokenStorage needs a real Android Context (Keystore) this pure-JVM
                // test doesn't have; substitutes an in-memory SecureStorage, mirroring the
                // MapSettings workaround above.
                module {
                    single<SecureStorage> {
                        object : SecureStorage {
                            private var stored: String? = null
                            override fun save(json: String) { stored = json }
                            override fun read(): String? = stored
                            override fun clear() { stored = null }
                        }
                    }
                    single { AuthLocalDataSource(storage = get()) }
                    single { AuthRemoteDataSource(httpClient = get()) }
                    single<AuthRepository> { AuthRepositoryImpl(remote = get(), local = get()) }
                    single { GoogleSignInLauncher() }
                    single { AppleSignInLauncher() }
                    factory { SignIn(authRepository = get(), entitlementRepository = get(), syncRepository = get()) }
                    factory { SignOut(authRepository = get(), entitlementRepository = get()) }
                    factory { DebugSimulateSignIn(authRepository = get(), entitlementRepository = get(), syncRepository = get()) }
                    factory { GetCurrentDisplayName(authRepository = get()) }
                    factory { GetCurrentUserProfile(authRepository = get()) }
                    viewModel {
                        LoginViewModel(
                            googleSignInLauncher = get(),
                            appleSignInLauncher = get(),
                            signIn = get(),
                            debugSimulateSignIn = get(),
                            getProfileBasics = get(),
                        )
                    }
                    viewModel { CompleteProfileViewModel(saveProfileBasics = get()) }
                },
                onboardingModule,
                exerciseLibraryModule,
                fullAssessmentModule,
                monthlyPlanModule,
                assessmentDetailModule,
                ptModeModule,
                chatAiModule,
                mealPlanModule,
                aiCustomPlanModule,
                planManagementModule,
                profileModule,
                profileSettingsModule,
                languageModule,
                syncModule,
            )
            checkModules()
        }
    }
}
