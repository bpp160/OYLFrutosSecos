package com.oyl.frutossecos

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Hash de demostración local: no reemplaza autenticación segura en el servidor. */
object PasswordHash {
    private const val ITERATIONS = 120_000
    fun create(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derive(password, salt)
        return "pbkdf2:$ITERATIONS:${Base64.encodeToString(salt, Base64.NO_WRAP)}:${Base64.encodeToString(hash, Base64.NO_WRAP)}"
    }
    fun matches(password: String, stored: String): Boolean {
        if (!stored.startsWith("pbkdf2:")) return password == stored // migración de demo anterior
        return try {
            val parts = stored.split(':')
            if (parts.size != 4) return false
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expected = Base64.decode(parts[3], Base64.NO_WRAP)
            val actual = derive(password, salt, parts[1].toInt())
            java.security.MessageDigest.isEqual(expected, actual)
        } catch (_: Exception) { false }
    }
    private fun derive(password: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
}
