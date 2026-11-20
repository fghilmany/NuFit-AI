package com.fghilmany.nufitai.usecase.appconfig

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.appconfig.entity.ForceUpdateInfo
import com.fghilmany.nufitai.domain.appconfig.repository.AppConfigRepository

/** Never blocks Home on a network/config failure -- an unreachable config means "don't force an
 * update", not "fail the screen". */
class CheckForceUpdate(private val repository: AppConfigRepository) {
    suspend operator fun invoke(): ForceUpdateInfo? = when (val result = repository.getForceUpdateInfo()) {
        is AppResult.Success -> result.data
        is AppResult.Error -> null
    }
}
