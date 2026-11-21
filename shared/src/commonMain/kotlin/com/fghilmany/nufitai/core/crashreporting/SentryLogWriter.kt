package com.fghilmany.nufitai.core.crashreporting

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import io.sentry.kotlin.multiplatform.Sentry

/** Forwards every `logger.e(...)`/`logger.a(...)` call already made across the repository layer
 * (SyncRepositoryImpl, ChatRepositoryImpl, etc. -- ~15 call sites) to Sentry, without touching any
 * of those call sites -- this is exactly the "Sentry sink can be added here later" AppLogger.kt
 * was left prepared for. Warn/info/debug stay Logcat/console-only (registered alongside
 * platformLogWriter() in KoinInit.kt), not sent to Sentry -- those aren't error signals. */
class SentryLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity != Severity.Error && severity != Severity.Assert) return
        if (throwable != null) {
            Sentry.captureException(throwable)
        } else {
            Sentry.captureMessage("[$tag] $message")
        }
    }
}
