package com.fghilmany.nufitai.domain.appconfig.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.appconfig.entity.ForceUpdateInfo

interface AppConfigRepository {
    /** Null = no update required (or config unreachable -- [AppResult.Error] handles that case). */
    suspend fun getForceUpdateInfo(): AppResult<ForceUpdateInfo?>
}
