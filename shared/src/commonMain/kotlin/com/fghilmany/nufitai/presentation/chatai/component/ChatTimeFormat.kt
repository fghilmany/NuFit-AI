package com.fghilmany.nufitai.presentation.chatai.component

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Mirrors `AccountScreen`'s existing HH:mm formatting precedent (no shared date-format utility exists yet). */
fun formatMessageTime(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}
