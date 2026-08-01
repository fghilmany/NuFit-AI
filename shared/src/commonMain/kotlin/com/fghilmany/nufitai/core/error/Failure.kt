package com.fghilmany.nufitai.core.error

sealed interface Failure {
    val message: String

    data class Network(override val message: String) : Failure
    data class Database(override val message: String) : Failure
    data class Validation(override val message: String) : Failure
    data class Unauthorized(override val message: String = "Session expired") : Failure

    /** techspec chat-ai.md §7/§8 -- distinguishable from a generic Network failure so the UI can show the upsell copy, not an error banner. */
    data class QuotaExceeded(override val message: String = "Quota exceeded") : Failure
}
