package com.porua.app.data.auth

import java.security.MessageDigest

/**
 * Utility for hashing user passwords securely using SHA-256 before storing locally.
 */
object PasswordHasher {
    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
