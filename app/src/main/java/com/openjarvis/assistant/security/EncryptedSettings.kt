package com.openjarvis.assistant.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Keeps provider credentials encrypted at rest using an Android Keystore AES key.
 * API keys are never embedded in source or BuildConfig.
 */
class EncryptedSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("jarvis_secure_settings", Context.MODE_PRIVATE)
    private val alias = "open_jarvis_settings_aes_v1"

    fun saveProvider(baseUrl: String, apiKey: String, model: String) {
        val payload = JSONObject()
            .put("baseUrl", baseUrl.trim().trimEnd('/'))
            .put("apiKey", apiKey.trim())
            .put("model", model.trim())
            .toString()
        val encrypted = encrypt(payload.toByteArray(Charsets.UTF_8))
        prefs.edit()
            .putString("provider_blob", Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun loadProvider(): ProviderSettings? {
        val blob = prefs.getString("provider_blob", null) ?: return null
        return runCatching {
            val raw = Base64.decode(blob, Base64.NO_WRAP)
            val json = JSONObject(String(decrypt(raw), Charsets.UTF_8))
            ProviderSettings(
                baseUrl = json.optString("baseUrl"),
                apiKey = json.optString("apiKey"),
                model = json.optString("model")
            )
        }.getOrNull()
    }

    private fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(data)
        return iv + ciphertext
    }

    private fun decrypt(data: ByteArray): ByteArray {
        require(data.size > 12) { "Encrypted provider settings are invalid." }
        val iv = data.copyOfRange(0, 12)
        val ciphertext = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}

data class ProviderSettings(
    val baseUrl: String,
    val apiKey: String,
    val model: String
)
