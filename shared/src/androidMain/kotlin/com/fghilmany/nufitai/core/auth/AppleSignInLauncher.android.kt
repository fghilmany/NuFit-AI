package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure

/** Apple sign-in is iOS-only; the Android actual exists only so this expect/actual compiles for both targets. */
actual class AppleSignInLauncher actual constructor() {
    actual suspend fun launch(): AppResult<String> =
        AppResult.Error(Failure.Network("Sign in with Apple is not available on Android"))
}
