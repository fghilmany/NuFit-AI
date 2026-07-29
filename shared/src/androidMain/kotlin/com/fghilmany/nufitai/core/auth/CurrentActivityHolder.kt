package com.fghilmany.nufitai.core.auth

import android.app.Activity
import java.lang.ref.WeakReference

/**
 * issue #83 follow-up (ADR-006) -- Credential Manager's `getCredential()` needs a live Activity
 * at call time, unlike `DatabaseDriverFactory`/`SettingsFactory`/`TokenStorage`, which only need
 * an Application context once at Koin startup. `MainActivity` registers/clears itself here via
 * `onResume`/`onPause`; [GoogleSignInLauncher] reads it at `launch()`-time. `WeakReference` avoids
 * leaking the Activity if it's destroyed without ever clearing this.
 */
object CurrentActivityHolder {
    private var ref: WeakReference<Activity>? = null

    var activity: Activity?
        get() = ref?.get()
        set(value) {
            ref = value?.let { WeakReference(it) }
        }
}
