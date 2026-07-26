package com.fghilmany.nufitai.core.settings

import com.russhwolf.settings.Settings

/** Mirrors `core/db/DatabaseDriverFactory`'s expect/actual shape -- Android needs a Context, iOS doesn't. */
expect class SettingsFactory {
    fun createSettings(): Settings
}
