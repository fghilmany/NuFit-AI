package com.fghilmany.nufitai.core.keepawake

import androidx.compose.runtime.Composable

/** Keeps the screen on while composed -- PT Mode's P-05 uses this so the device doesn't dim/lock mid-session (DoD). */
@Composable
expect fun KeepScreenOn()
