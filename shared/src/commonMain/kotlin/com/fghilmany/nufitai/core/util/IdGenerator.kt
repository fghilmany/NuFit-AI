package com.fghilmany.nufitai.core.util

import kotlin.random.Random

fun generateId(): String {
    val timestamp = currentInstant().toEpochMilliseconds()
    val randomSuffix = Random.nextInt(0, Int.MAX_VALUE)
    return "$timestamp-$randomSuffix"
}
