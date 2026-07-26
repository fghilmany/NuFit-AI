package com.fghilmany.nufitai.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "nufit_auth_session_key"
private const val PREFS_NAME = "nufit_secure_token"
private const val PREF_KEY_CIPHERTEXT = "session_ciphertext"
private const val PREF_KEY_IV = "session_iv"
private const val GCM_TAG_LENGTH_BITS = 128

/**
 * Encrypts the session JSON with an AES key generated inside the Android Keystore (the raw key
 * material never leaves secure hardware) before persisting the ciphertext via SharedPreferences.
 */
actual class TokenStorage(context: Context) : SecureStorage {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    actual override fun save(json: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(json.encodeToByteArray())
        prefs.edit()
            .putString(PREF_KEY_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .putString(PREF_KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    actual override fun read(): String? {
        val ciphertextB64 = prefs.getString(PREF_KEY_CIPHERTEXT, null) ?: return null
        val ivB64 = prefs.getString(PREF_KEY_IV, null) ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(ivB64, Base64.NO_WRAP)))
        return cipher.doFinal(Base64.decode(ciphertextB64, Base64.NO_WRAP)).decodeToString()
    }

    actual override fun clear() {
        prefs.edit().remove(PREF_KEY_CIPHERTEXT).remove(PREF_KEY_IV).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }
}
