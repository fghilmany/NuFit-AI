package com.fghilmany.nufitai.core.security

/**
 * Plain interface [TokenStorage] implements -- lets `DiVerificationTest` (pure JVM, no Android
 * Context available) substitute an in-memory fake instead of constructing a real
 * Keystore/Keychain-backed [TokenStorage], the same way `EntitlementLocalDataSource` depends on
 * the `Settings` interface rather than a concrete platform class.
 */
interface SecureStorage {
    fun save(json: String)
    fun read(): String?
    fun clear()
}
