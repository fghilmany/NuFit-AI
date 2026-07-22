package com.fghilmany.nufitai.core.error

sealed interface Failure {
    val message: String

    data class Network(override val message: String) : Failure
    data class Database(override val message: String) : Failure
    data class Validation(override val message: String) : Failure
    data class Unauthorized(override val message: String = "Session expired") : Failure
}
