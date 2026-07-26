package com.fghilmany.nufitai.core.auth

import com.fghilmany.nufitai.core.error.AppResult

/**
 * techspec §3 (auth-sync.md, issue #83) -- native ID-token retrieval is stubbed on both
 * platforms pending real OAuth client IDs (TODO(dev) in each actual). Returns the ID token on
 * success; an [AppResult.Error] on cancellation/failure/not-yet-configured.
 */
expect class GoogleSignInLauncher() {
    suspend fun launch(): AppResult<String>
}
