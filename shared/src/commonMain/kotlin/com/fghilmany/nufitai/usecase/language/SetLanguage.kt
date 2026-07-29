package com.fghilmany.nufitai.usecase.language

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.language.LocaleController
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.domain.language.repository.LanguageRepository

class SetLanguage(
    private val repository: LanguageRepository,
    private val localeController: LocaleController,
) {
    suspend operator fun invoke(language: AppLanguage): AppResult<Unit> {
        val result = repository.setLanguage(language)
        if (result is AppResult.Success) {
            localeController.applyLocale(language)
        }
        return result
    }
}
