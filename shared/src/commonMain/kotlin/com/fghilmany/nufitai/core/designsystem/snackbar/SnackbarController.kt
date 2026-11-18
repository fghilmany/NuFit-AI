package com.fghilmany.nufitai.core.designsystem.snackbar

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Dev-requested (18 Nov 2026): errors were rendering as raw inline Text instead of a
 * toast/snackbar/dialog, and the app had zero Snackbar infrastructure anywhere. A plain object
 * (not a Koin-injected class) is deliberate -- ViewModels call [showError] directly without
 * needing a SnackbarHostState threaded through every constructor; [NuFitNavGraph] is the single
 * place that collects [messages] and actually shows them.
 */
object SnackbarController {
    private val channel = Channel<String>(Channel.BUFFERED)
    val messages = channel.receiveAsFlow()

    fun showError(message: String) {
        channel.trySend(message)
    }
}
