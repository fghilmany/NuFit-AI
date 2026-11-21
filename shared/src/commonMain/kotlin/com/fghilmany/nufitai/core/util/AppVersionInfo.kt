package com.fghilmany.nufitai.core.util

/** Must match androidApp/build.gradle.kts defaultConfig.versionCode -- bump both together on
 * every release, or the force-update check (CheckForceUpdate) compares against a stale number. */
const val CURRENT_VERSION_CODE = 1

/** Must match androidApp/build.gradle.kts defaultConfig.versionName -- display-only (About/Account
 * screens), never compared against anything. Bump alongside CURRENT_VERSION_CODE so both version
 * constants move together instead of the display string quietly going stale. */
const val APP_VERSION_NAME = "0.0.1"
