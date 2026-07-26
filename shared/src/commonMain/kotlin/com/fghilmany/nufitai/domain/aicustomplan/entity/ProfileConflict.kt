package com.fghilmany.nufitai.domain.aicustomplan.entity

/**
 * AC-9 -- surfaced as a non-blocking preview banner, never blocks generation.
 * [messageKey] is a Compose string-resource key, not raw text (architecture.md §3: never
 * persist/generate user-facing text outside presentation).
 */
data class ProfileConflict(
    val field: String,
    val messageKey: String,
)
