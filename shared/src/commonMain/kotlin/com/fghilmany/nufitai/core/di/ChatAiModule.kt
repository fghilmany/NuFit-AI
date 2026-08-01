package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.chatai.datasource.ChatExerciseOverrideLocalDataSource
import com.fghilmany.nufitai.data.chatai.datasource.ChatLocalDataSource
import com.fghilmany.nufitai.data.chatai.datasource.ChatQuotaLocalDataSource
import com.fghilmany.nufitai.data.chatai.datasource.ChatRemoteDataSource
import com.fghilmany.nufitai.data.chatai.repository.ChatExerciseOverrideRepositoryImpl
import com.fghilmany.nufitai.data.chatai.repository.ChatRepositoryImpl
import com.fghilmany.nufitai.domain.chatai.repository.ChatExerciseOverrideRepository
import com.fghilmany.nufitai.domain.chatai.repository.ChatRepository
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ChatGateViewModel
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ChatViewModel
import com.fghilmany.nufitai.usecase.chatai.ApplyChatExerciseSuggestion
import com.fghilmany.nufitai.usecase.chatai.GetChatHistory
import com.fghilmany.nufitai.usecase.chatai.GetChatQuota
import com.fghilmany.nufitai.usecase.chatai.ReconcileStuckChatMessages
import com.fghilmany.nufitai.usecase.chatai.RetryFailedMessage
import com.fghilmany.nufitai.usecase.chatai.SendChatMessage
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. techspec chat-ai.md §6 -- the "Terapkan ke Plan" override
 * plumbing, the full ChatRepository data-layer stack (Stage C), and the presentation layer
 * (Stage D: gate + chat ViewModels) are all wired here now.
 */
val chatAiModule = module {
    single { ChatExerciseOverrideLocalDataSource(database = get()) }
    single<ChatExerciseOverrideRepository> { ChatExerciseOverrideRepositoryImpl(local = get()) }

    single { ChatLocalDataSource(database = get()) }
    single { ChatQuotaLocalDataSource(settings = get()) }
    single { ChatRemoteDataSource(httpClient = get()) }
    single<ChatRepository> { ChatRepositoryImpl(local = get(), quotaLocal = get(), remote = get(), authRepository = get()) }

    // GetActivePlanOverview/GetSessionStatus registered in monthlyPlanModule, GetCurrentTier in
    // entitlementModule, GetExerciseDetail in exerciseLibraryModule -- all load together in
    // KoinInit, so cross-module `get()` here resolves fine.
    factory { ApplyChatExerciseSuggestion(getActivePlanOverview = get(), getSessionStatus = get(), overrideRepository = get()) }
    factory { GetChatHistory(repository = get()) }
    factory { SendChatMessage(repository = get()) }
    factory { RetryFailedMessage(repository = get()) }
    factory { GetChatQuota(repository = get()) }
    factory { ReconcileStuckChatMessages(repository = get()) }

    viewModel { ChatGateViewModel(getCurrentTier = get()) }
    viewModel {
        ChatViewModel(
            reconcileStuckChatMessages = get(),
            getChatHistory = get(),
            sendChatMessage = get(),
            retryFailedMessage = get(),
            getChatQuota = get(),
            applyChatExerciseSuggestion = get(),
            getExerciseDetail = get(),
        )
    }
}
