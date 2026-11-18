package com.fghilmany.nufitai.core.share

import android.content.Intent
import com.fghilmany.nufitai.core.auth.CurrentActivityHolder

/** Reuses [CurrentActivityHolder] (already tracked by `MainActivity` for Google Sign-In) --
 * starting `ACTION_SEND` needs a live Activity the same way `getCredential()` does. */
actual class ShareLauncher actual constructor() {
    actual fun shareText(text: String) {
        val activity = CurrentActivityHolder.activity ?: return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(intent, null))
    }
}
