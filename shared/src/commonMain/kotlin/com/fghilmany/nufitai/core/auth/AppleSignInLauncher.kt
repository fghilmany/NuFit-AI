package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult

/** techspec §3 (auth-sync.md, issue #83) -- stubbed pending real ASAuthorizationController wiring on iOS. */
expect class AppleSignInLauncher() {
    suspend fun launch(): AppResult<NativeSignInResult>
}
