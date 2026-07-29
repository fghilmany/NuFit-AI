package com.fghilmany.nufitai.data.language.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.language.datasource.LanguageLocalDataSource
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.domain.language.repository.LanguageRepository

class LanguageRepositoryImpl(private val local: LanguageLocalDataSource) : LanguageRepository {
    override suspend fun getLanguage(): AppResult<AppLanguage> = runCatchingDatabase {
        AppLanguage.entries.find { it.tag == local.getLanguageTag() } ?: AppLanguage.INDONESIAN
    }

    override suspend fun setLanguage(language: AppLanguage): AppResult<Unit> = runCatchingDatabase {
        local.setLanguageTag(language.tag)
    }
}
