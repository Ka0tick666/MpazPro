
package com.duoc.mpazpro.data

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERACIONES = 120_000
    private const val LONGITUD_HASH = 256
    private val random = SecureRandom()

    fun generarHash(contrasena: String): String {
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val hash = calcularHash(contrasena, salt)

        return "${salt.aHexadecimal()}:${hash.aHexadecimal()}"
    }

    fun verificar(contrasena: String, hashGuardado: String): Boolean {
        val partes = hashGuardado.split(":")
        if (partes.size != 2) return false

        return try {
            val salt = partes[0].desdeHexadecimal()
            val hashOriginal = partes[1].desdeHexadecimal()
            val hashIngresado = calcularHash(contrasena, salt)

            MessageDigest.isEqual(hashOriginal, hashIngresado)
        } catch (_: Exception) {
            false
        }
    }

    private fun calcularHash(
        contrasena: String,
        salt: ByteArray
    ): ByteArray {
        val especificacion = PBEKeySpec(
            contrasena.toCharArray(),
            salt,
            ITERACIONES,
            LONGITUD_HASH
        )

        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(especificacion)
                .encoded
        } finally {
            especificacion.clearPassword()
        }
    }

    private fun ByteArray.aHexadecimal(): String =
        joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun String.desdeHexadecimal(): ByteArray {
        require(length % 2 == 0)
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
