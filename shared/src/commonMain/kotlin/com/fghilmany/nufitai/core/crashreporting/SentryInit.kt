package com.fghilmany.nufitai.core.crashreporting

import io.sentry.kotlin.multiplatform.Sentry

// Sentry project "nufit-ai" in org "faris-ghilmany-studio" (sentry.io). A DSN is not a secret --
// Sentry's own docs say to put it directly in the init call, same as this codebase already does
// for the Supabase anon key -- it can only submit events, never read data.
private const val SENTRY_DSN = "https://33e9f97203aaf88003471019fd0d75f4@o4512462031749120.ingest.us.sentry.io/4512462508457984"

/** Crash reporting only (tech-stack.md's locked choice: Sentry KMP, not Crashlytics, for
 * Kotlin/Native iOS symbolication). Called once from `initKoin` -- no tracing/profiling/replay
 * enabled, that wasn't asked for. */
fun initSentry(isDebugBuild: Boolean) {
    Sentry.init { options ->
        options.dsn = SENTRY_DSN
        options.environment = if (isDebugBuild) "debug" else "release"
        // Prints the SDK's own verbose init/send diagnostics to Logcat (tag "Sentry") -- the
        // only way to tell "init silently no-op'd" apart from "init worked, event just hasn't
        // shown up in the dashboard yet" (dev report 21 Nov 2026: neither logs nor dashboard
        // showed anything after tapping the test-crash button).
        options.debug = isDebugBuild
    }
}
