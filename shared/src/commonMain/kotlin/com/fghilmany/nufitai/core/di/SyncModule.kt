package com.fghilmany.nufitai.core.di

import com.fghilmany.nufitai.data.fullassessment.sync.FullAssessmentSyncParticipant
import com.fghilmany.nufitai.data.monthlyplan.sync.MonthlyPlanSyncParticipant
import com.fghilmany.nufitai.data.onboarding.sync.BodyMeasurementSyncParticipant
import com.fghilmany.nufitai.data.onboarding.sync.ParQSyncParticipant
import com.fghilmany.nufitai.data.onboarding.sync.QuickAssessmentSyncParticipant
import com.fghilmany.nufitai.data.ptmode.sync.WorkoutHistorySyncParticipant
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.data.sync.datasource.SyncCursorLocalDataSource
import com.fghilmany.nufitai.data.sync.datasource.SyncRemoteDataSource
import com.fghilmany.nufitai.data.sync.repository.SyncRepositoryImpl
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository
import com.fghilmany.nufitai.usecase.sync.ObserveSyncStatus
import com.fghilmany.nufitai.usecase.sync.SyncNow
import org.koin.dsl.module

/**
 * Manual Koin DSL, per ADR-001. `List<SyncParticipant>` is assembled by hand -- Koin's manual
 * DSL has no auto-multibinding (techspec §7).
 */
val syncModule = module {
    single { MonthlyPlanSyncParticipant(local = get()) }
    single { FullAssessmentSyncParticipant() }
    single { QuickAssessmentSyncParticipant() }
    single { ParQSyncParticipant() }
    single { BodyMeasurementSyncParticipant() }
    single { WorkoutHistorySyncParticipant() }
    single<List<SyncParticipant>> {
        listOf(
            get<MonthlyPlanSyncParticipant>(),
            get<FullAssessmentSyncParticipant>(),
            get<QuickAssessmentSyncParticipant>(),
            get<ParQSyncParticipant>(),
            get<BodyMeasurementSyncParticipant>(),
            get<WorkoutHistorySyncParticipant>(),
        )
    }

    single { SyncRemoteDataSource(httpClient = get()) }
    single { SyncCursorLocalDataSource(settings = get()) }
    single<SyncRepository> { SyncRepositoryImpl(participants = get(), remote = get(), cursor = get(), authRepository = get()) }

    factory { SyncNow(repository = get()) }
    factory { ObserveSyncStatus(repository = get()) }
}
