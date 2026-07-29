package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.core.auth.AppleSignInLauncher
import com.fghilmany.nufitai.core.auth.GoogleSignInLauncher
import com.fghilmany.nufitai.core.security.SecureStorage
import com.fghilmany.nufitai.core.security.TokenStorage
import com.fghilmany.nufitai.data.auth.datasource.AuthLocalDataSource
import com.fghilmany.nufitai.data.auth.datasource.AuthRemoteDataSource
import com.fghilmany.nufitai.data.auth.repository.AuthRepositoryImpl
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.presentation.auth.viewmodel.CompleteProfileViewModel
import com.fghilmany.nufitai.presentation.auth.viewmodel.LoginViewModel
import com.fghilmany.nufitai.usecase.auth.DebugSimulateSignIn
import com.fghilmany.nufitai.usecase.auth.GetCurrentDisplayName
import com.fghilmany.nufitai.usecase.auth.GetCurrentUserProfile
import com.fghilmany.nufitai.usecase.auth.SignIn
import com.fghilmany.nufitai.usecase.auth.SignOut
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. [TokenStorage] is an expect/actual class built differently per
 * platform (Android needs a Context), so its instance is handed in explicitly by each platform's
 * entry point via `initKoin`, mirroring `entitlementModule(settingsFactory)`.
 */
fun authModule(tokenStorage: TokenStorage) = module {
    single<SecureStorage> { tokenStorage }
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
}
