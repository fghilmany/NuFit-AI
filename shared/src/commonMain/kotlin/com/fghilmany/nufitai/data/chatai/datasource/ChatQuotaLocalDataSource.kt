package com.fghilmany.nufitai.data.chatai.datasource

import com.russhwolf.settings.Settings

private const val KEY_USED = "chat_quota_used"
private const val KEY_MAX = "chat_quota_max"
private const val KEY_RESETS_AT_EPOCH_MILLIS = "chat_quota_resets_at_epoch_millis"

/**
 * techspec chat-ai.md §5 -- caches the last quota the server reported so `GetChatQuota` has
 * something to show without needing a fresh send; always overwritten by the next response,
 * never computed/decremented locally (mirrors `EntitlementLocalDataSource`'s Settings pattern).
 */
class ChatQuotaLocalDataSource(private val settings: Settings) {
    fun getQuota(): Triple<Int, Int, Long>? {
        val max = settings.getIntOrNull(KEY_MAX) ?: return null
        val used = settings.getInt(KEY_USED, 0)
        val resetsAt = settings.getLong(KEY_RESETS_AT_EPOCH_MILLIS, 0L)
        return Triple(used, max, resetsAt)
    }

    fun setQuota(used: Int, max: Int, resetsAtEpochMillis: Long) {
        settings.putInt(KEY_USED, used)
        settings.putInt(KEY_MAX, max)
        settings.putLong(KEY_RESETS_AT_EPOCH_MILLIS, resetsAtEpochMillis)
    }
}
