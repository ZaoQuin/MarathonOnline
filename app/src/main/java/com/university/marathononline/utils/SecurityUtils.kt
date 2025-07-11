package com.university.marathononline.utils

import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

fun createOrGetKey(): SecretKey {
    val keyStore = KeyStore.getInstance("AndroidKeyStore")
    keyStore.load(null)

    val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
    if (existingKey != null) {
        return existingKey
    }

    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        "AndroidKeyStore"
    )

    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build()

    keyGenerator.init(keyGenParameterSpec)
    return keyGenerator.generateKey()
}
