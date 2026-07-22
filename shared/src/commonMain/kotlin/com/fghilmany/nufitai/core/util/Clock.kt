package com.fghilmany.nufitai.core.util

import kotlin.time.Instant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun currentInstant(): Instant = Clock.System.now()
