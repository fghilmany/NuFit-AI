package com.fghilmany.nufitai.di

import org.koin.core.module.Module
import org.koin.ksp.generated.module

actual fun platformModules(): List<Module> = listOf(AppModule().module)
