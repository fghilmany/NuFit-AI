package com.fghilmany.nufitai.core.language

import com.fghilmany.nufitai.domain.language.entity.AppLanguage

/** Applies [AppLanguage] at the platform level so Compose resource lookups follow it. */
expect class LocaleController() {
    fun applyLocale(language: AppLanguage)
}
