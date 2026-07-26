package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure

/**
 * TODO(dev): wire Android Credential Manager (GetGoogleIdOption) here. Needs an Activity Context
 * -- plumb it in the same way `DatabaseDriverFactory(applicationContext)` is constructed at the
 * app entry point and handed into `initKoin`, once real Google OAuth client IDs exist.
 */
actual class GoogleSignInLauncher actual constructor() {
    actual suspend fun launch(): AppResult<String> =
        AppResult.Error(Failure.Network("Native Google sign-in not yet configured"))
}
