package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure

/** TODO(dev): wire Google Sign-In iOS SDK here once real OAuth client IDs exist. */
actual class GoogleSignInLauncher actual constructor() {
    actual suspend fun launch(): AppResult<String> =
        AppResult.Error(Failure.Network("Native Google sign-in not yet configured"))
}
