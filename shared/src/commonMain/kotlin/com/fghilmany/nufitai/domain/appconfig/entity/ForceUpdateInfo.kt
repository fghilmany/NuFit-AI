package com.fghilmany.nufitai.domain.appconfig.entity

/** Non-null means the installed app is below `min_android_version_code` -- Home must show a
 * blocking "update required" overlay linking to [storeUrl]. */
data class ForceUpdateInfo(val storeUrl: String)
