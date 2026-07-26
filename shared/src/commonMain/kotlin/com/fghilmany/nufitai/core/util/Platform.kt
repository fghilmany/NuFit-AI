package com.fghilmany.nufitai.core.util

/** techspec §9 item 17 (auth-sync.md) -- Sign in with Apple must show on iOS, never on Android. */
expect fun isIOSPlatform(): Boolean
