package com.alaa.iptv.data.preferences

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * تشفير بيانات الاشتراك بمفتاح AES-GCM محفوظ داخل Android Keystore.
 * يتطلب التطبيق Android 6.0+ لضمان عدم وجود مسار تخزين غير مشفّر.
 */
class CredentialCrypto {
    companion object {
        private const val PREFIX = "v1:"
        private const val KEY_ALIAS = "alaa_iptv_credential_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    fun encrypt(value: String): String {
        if (value.isBlank() || isEncrypted(value)) return value
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            }
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            "$PREFIX${Base64.encodeToString(cipher.iv, Base64.NO_WRAP)}:${Base64.encodeToString(encrypted, Base64.NO_WRAP)}"
        }.getOrDefault(value)
    }

    fun decrypt(value: String): String {
        if (!isEncrypted(value)) return value
        return runCatching {
            val parts = value.removePrefix(PREFIX).split(":", limit = 2)
            require(parts.size == 2)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP))
                )
            }
            String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrDefault("")
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }
}
