package com.fghilmany.nufitai.data.appconfig.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.core.logging.AppLogger
import com.fghilmany.nufitai.core.util.CURRENT_VERSION_CODE
import com.fghilmany.nufitai.data.appconfig.datasource.AppConfigRemoteDataSource
import com.fghilmany.nufitai.domain.appconfig.entity.ForceUpdateInfo
import com.fghilmany.nufitai.domain.appconfig.repository.AppConfigRepository
import kotlinx.coroutines.CancellationException

class AppConfigRepositoryImpl(
    private val remote: AppConfigRemoteDataSource,
) : AppConfigRepository {
    private val logger = AppLogger.tagged("AppConfigRepository")

    override suspend fun getForceUpdateInfo(): AppResult<ForceUpdateInfo?> = try {
        val config = remote.getConfig()
        val minVersionCode = config["min_android_version_code"]?.toIntOrNull()
        val storeUrl = config["android_store_url"]
        val required = minVersionCode != null && storeUrl != null && CURRENT_VERSION_CODE < minVersionCode
        AppResult.Success(if (required) ForceUpdateInfo(storeUrl!!) else null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.e(e) { "getForceUpdateInfo failed -- ${e.message}" }
        AppResult.Error(Failure.Network("Couldn't check for updates."))
    }
}
