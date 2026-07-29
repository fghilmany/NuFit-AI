package com.fghilmany.nufitai.usecase.language

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import com.fghilmany.nufitai.domain.language.repository.LanguageRepository

class GetLanguage(private val repository: LanguageRepository) {
    suspend operator fun invoke(): AppResult<AppLanguage> = repository.getLanguage()
}
