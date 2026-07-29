package com.fghilmany.nufitai.data.profilesettings.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.data.profilesettings.datasource.AccountDeletionApiException
import com.fghilmany.nufitai.data.profilesettings.datasource.AccountDeletionRemoteDataSource
import com.fghilmany.nufitai.data.profilesettings.datasource.ProfileSettingsLocalDataSource
import com.fghilmany.nufitai.domain.auth.repository.AuthRepository
import com.fghilmany.nufitai.domain.profilesettings.repository.ProfileSettingsRepository
import kotlinx.coroutines.CancellationException

class ProfileSettingsRepositoryImpl(
    private val local: ProfileSettingsLocalDataSource,
    private val remote: AccountDeletionRemoteDataSource,
    private val authRepository: AuthRepository,
) : ProfileSettingsRepository {
    private val logger = AppLogger.tagged("ProfileSettingsRepository")

    override suspend fun hasSeenMedicalDisclaimer(): AppResult<Boolean> = runCatchingDatabase {
        local.hasSeenMedicalDisclaimer()
    }

    override suspend fun markMedicalDisclaimerSeen(): AppResult<Unit> = runCatchingDatabase {
        local.markMedicalDisclaimerSeen()
    }

    override suspend fun deleteAccount(): AppResult<Unit> {
        val session = authRepository.getCurrentSession()
            ?: run {
                logger.w { "deleteAccount aborted -- no active session" }
                return AppResult.Error(Failure.Unauthorized())
            }
        logger.i { "deleteAccount starting for userId=${session.userId}" }
        return try {
            remote.deleteAccount(session.accessToken)
            logger.i { "deleteAccount succeeded" }
            AppResult.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AccountDeletionApiException) {
            logger.e(e) { "deleteAccount failed -- HTTP ${e.statusCode}" }
            when (e.statusCode) {
                401 -> AppResult.Error(Failure.Unauthorized())
                else -> AppResult.Error(Failure.Network("Delete-account backend error (${e.statusCode})"))
            }
        } catch (e: Exception) {
            logger.e(e) { "deleteAccount failed -- ${e.message}" }
            AppResult.Error(Failure.Network(e.message ?: "Unknown network error"))
        }
    }
}
