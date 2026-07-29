package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure

/** TODO(dev): wire ASAuthorizationController (Sign in with Apple) here once configured. */
actual class AppleSignInLauncher actual constructor() {
    actual suspend fun launch(): AppResult<NativeSignInResult> =
        AppResult.Error(Failure.Network("Native Apple sign-in not yet configured"))
}
