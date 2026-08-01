package com.fghilmany.nufitai.domain.chatai.entity

import kotlin.time.Instant

/** techspec chat-ai.md §5 -- always server-authoritative; the client never decrements this locally. */
data class ChatQuota(val used: Int, val max: Int, val resetsAt: Instant)
