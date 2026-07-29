package com.fghilmany.nufitai.data.sync.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.core.util.currentInstant
import com.fghilmany.nufitai.data.sync.contract.SyncParticipant
import com.fghilmany.nufitai.data.sync.datasource.SyncApiException
import com.fghilmany.nufitai.data.sync.datasource.SyncCursorLocalDataSource
import com.fghilmany.nufitai.data.sync.datasource.SyncRemoteDataSource
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.domain.sync.repository.SyncRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Instant

/**
 * techspec §5/§7 (auth-sync.md) -- orchestrates push-then-pull across all [SyncParticipant]s,
 * timestamp-cursor based, LWW per record (each participant compares `updated_at` itself when
 * applying pulled rows). A no-op (Idle, no error) when there's no active session -- guests never
 * trigger network calls.
 */
class SyncRepositoryImpl(
    private val participants: List<SyncParticipant>,
    private val remote: SyncRemoteDataSource,
    private val cursor: SyncCursorLocalDataSource,
    private val authRepository: AuthRepository,
) : SyncRepository {
    private val logger = AppLogger.tagged("SyncRepository")
    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    override suspend fun syncNow(): AppResult<Unit> {
        val session = when (val refreshed = authRepository.refreshSessionIfNeeded()) {
            is AppResult.Error -> {
                logger.w { "Aborting sync -- session refresh failed: ${refreshed.failure}" }
                _status.value = SyncStatus.Failed(currentInstant(), refreshed.failure)
                return AppResult.Error(refreshed.failure)
            }
            is AppResult.Success -> refreshed.data
        } ?: run {
            logger.d { "Skipping sync -- no active session (guest)" }
            return AppResult.Success(Unit)
        }

        logger.i { "Sync starting for user=${session.userId}, ${participants.size} participants: ${participants.map { it.tableName }}" }
        _status.value = SyncStatus.Syncing
        return try {
            participants.forEach { it.backfillUserId(session.userId) }

            val since = Instant.fromEpochMilliseconds(cursor.getLastSyncedAtEpochMillis())
            logger.d { "Pushing changes since $since" }
            participants.forEach { participant ->
                val changed = participant.getChangedSince(since)
                logger.d { "Push ${participant.tableName}: ${changed.size} row(s)" }
                remote.push(participant.tableName, changed, session.accessToken)
            }

            val sinceEpochMillis = since.toEpochMilliseconds()
            participants.forEach { participant ->
                logger.d { "Pull ${participant.tableName}" }
                val remoteRows = remote.pull(participant.tableName, session.userId, sinceEpochMillis, session.accessToken)
                logger.d { "Pulled ${participant.tableName}: ${remoteRows.size} row(s)" }
                participant.upsertRemoteRows(remoteRows)
            }

            val now = currentInstant()
            cursor.setLastSyncedAtEpochMillis(now.toEpochMilliseconds())
            _status.value = SyncStatus.Success(now)
            logger.i { "Sync succeeded at $now" }
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SyncApiException) {
            val failure = if (e.statusCode == 401) Failure.Unauthorized() else Failure.Network("Sync backend error (${e.statusCode})")
            logger.e(e) { "Sync failed -- HTTP ${e.statusCode}" }
            _status.value = SyncStatus.Failed(currentInstant(), failure)
            AppResult.Error(failure)
        } catch (e: Exception) {
            val failure = Failure.Network(e.message ?: "Unknown network error")
            logger.e(e) { "Sync failed -- ${e.message}" }
            _status.value = SyncStatus.Failed(currentInstant(), failure)
            AppResult.Error(failure)
        }
    }
}
