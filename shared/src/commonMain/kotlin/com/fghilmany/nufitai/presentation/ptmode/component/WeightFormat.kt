package com.fghilmany.nufitai.presentation.ptmode.component

import kotlin.math.round

/** "20.0" -> "20", "22.5" -> "22.5" -- avoids java.util.Formatter (not KMP-common-safe). */
fun formatWeightKg(kg: Double): String {
    val rounded = round(kg * 10) / 10
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
