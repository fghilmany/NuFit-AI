package com.fghilmany.nufitai.core.share

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual class ShareLauncher actual constructor() {
    actual fun shareText(text: String) {
        val keyWindow = UIApplication.sharedApplication.windows
            .filterIsInstance<UIWindow>()
            .firstOrNull { it.isKeyWindow() }
        val rootViewController = keyWindow?.rootViewController ?: return
        val activityViewController = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
        rootViewController.presentViewController(activityViewController, animated = true, completion = null)
    }
}
