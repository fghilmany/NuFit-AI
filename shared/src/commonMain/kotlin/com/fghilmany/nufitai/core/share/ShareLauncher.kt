package com.fghilmany.nufitai.core.share

/** Dev report (18 Nov) -- share a chat message's text via the OS share sheet. */
expect class ShareLauncher() {
    fun shareText(text: String)
}
