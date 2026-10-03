package com.lovely.gweather.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class HashedCredential(
    val hash: String,
    val salt: String
)

object CredentialHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    fun hash(password: String): HashedCredential {
        val salt = ByteArray(SALT_LENGTH_BYTES).also(SecureRandom()::nextBytes)
        return HashedCredential(
            hash = derive(password, salt),
            salt = Base64.getEncoder().encodeToString(salt)
        )
    }

    fun verify(password: String, expectedHash: String, encodedSalt: String): Boolean {
        return try {
            val salt = Base64.getDecoder().decode(encodedSalt)
            val expected = Base64.getDecoder().decode(expectedHash)
            val actual = Base64.getDecoder().decode(derive(password, salt))
            MessageDigest.isEqual(expected, actual)
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun derive(password: String, salt: ByteArray): String {
        val specification = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            Base64.getEncoder().encodeToString(
                SecretKeyFactory.getInstance(ALGORITHM).generateSecret(specification).encoded
            )
        } finally {
            specification.clearPassword()
        }
    }
}