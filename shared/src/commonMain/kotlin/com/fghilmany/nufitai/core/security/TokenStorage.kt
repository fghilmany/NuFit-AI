package com.fghilmany.nufitai.core.security

/**
 * techspec §3/§6 (auth-sync.md, issue #83) -- encrypts the session token at rest via platform
 * Keystore (Android) / Keychain (iOS), no new dependency. Mirrors `SettingsFactory`'s
 * expect/actual shape (Android needs a Context, iOS doesn't).
 */
expect class TokenStorage : SecureStorage {
    override fun save(json: String)
    override fun read(): String?
    override fun clear()
}
