package com.fghilmany.nufitai.core.auth

/** Shared result shape for [GoogleSignInLauncher]/[AppleSignInLauncher] -- fields are best-effort, may be null. */
data class NativeSignInResult(val idToken: String, val displayName: String?, val profilePictureUrl: String?)
