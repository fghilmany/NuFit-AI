package com.fghilmany.nufitai.core.logging

import co.touchlab.kermit.Logger

/**
 * Thin Kermit wrapper (architecture.md §2 `core/logging/`) -- `libs.kermit` was already in the
 * version catalog and `shared/build.gradle.kts`, but nothing in the codebase ever called it
 * (confirmed via repo-wide search), so every network/auth/sync failure was silently swallowed
 * into a UI error string with no trace of what actually happened underneath. This gives every
 * layer a one-line way to get a tagged logger; a Sentry sink (tech-stack.md's eventual
 * integration) can be added here later without touching call sites.
 */
object AppLogger {
    fun tagged(tag: String): Logger = Logger.withTag(tag)
}
