package com.fghilmany.nufitai.data.chatai.datasource

import com.russhwolf.settings.Settings

private const val KEY_STARTED_AFTER_EPOCH_MILLIS = "chat_conversation_started_after_epoch_millis"

/** "New Chat" cutoff (dev-requested, 2026-11-18) -- messages created before this instant are
 * hidden from the UI and excluded from the model's context, without deleting anything. Mirrors
 * [ChatQuotaLocalDataSource]'s Settings pattern. Null = no cutoff set, full history shown. */
class ChatConversationLocalDataSource(private val settings: Settings) {
    fun getStartedAfterEpochMillis(): Long? = settings.getLongOrNull(KEY_STARTED_AFTER_EPOCH_MILLIS)

    fun setStartedAfterEpochMillis(epochMillis: Long) {
        settings.putLong(KEY_STARTED_AFTER_EPOCH_MILLIS, epochMillis)
    }
}
