package com.fghilmany.nufitai.core.security

import kotlinx.cinterop.CFBridgingRelease
import kotlinx.cinterop.CFBridgingRetain
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.CFTypeRefVar

private const val SERVICE = "com.fghilmany.nufitai.auth"
private const val ACCOUNT = "session"

/**
 * Stores the session JSON as a generic password item in the iOS Keychain.
 *
 * TODO(dev): this is the first Keychain/Security-framework cinterop code in this repo (no
 * existing pattern to mirror -- `SettingsFactory.ios.kt` uses NSUserDefaults, not Keychain) and
 * could not be compiled/verified without a Kotlin/Native toolchain. Double-check the
 * `CFDictionaryOf`/`CFBridgingRetain` cinterop calls compile as written; adjust signatures if
 * the Security.def bindings differ on your Kotlin/Native version.
 */
@OptIn(ExperimentalForeignApi::class)
actual class TokenStorage : SecureStorage {
    actual override fun save(json: String) {
        clear()
        val data = (json as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to CFBridgingRetain(SERVICE),
            kSecAttrAccount to CFBridgingRetain(ACCOUNT),
            kSecValueData to CFBridgingRetain(data),
        )
        SecItemAdd(query, null)
    }

    actual override fun read(): String? = memScoped {
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to CFBridgingRetain(SERVICE),
            kSecAttrAccount to CFBridgingRetain(ACCOUNT),
            kSecReturnData to CFBridgingRetain(true),
            kSecMatchLimit to kSecMatchLimitOne,
        )
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status != errSecSuccess) return@memScoped null
        val data = result.value?.let { CFBridgingRelease(it) as? NSData } ?: return@memScoped null
        NSString.create(data = data, encoding = NSUTF8StringEncoding) as String?
    }

    actual override fun clear() {
        val query = CFDictionaryOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to CFBridgingRetain(SERVICE),
            kSecAttrAccount to CFBridgingRetain(ACCOUNT),
        )
        SecItemDelete(query)
    }
}
