package com.fghilmany.nufitai.domain.language.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.language.entity.AppLanguage

interface LanguageRepository {
    suspend fun getLanguage(): AppResult<AppLanguage>
    suspend fun setLanguage(language: AppLanguage): AppResult<Unit>
}
