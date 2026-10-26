
package com.duoc.mpazpro.data

import android.database.sqlite.SQLiteConstraintException

data class ResultadoOperacion(
    val exito: Boolean,
    val mensaje: String,
    val usuario: Usuario? = null
)

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        rol: String
    ): ResultadoOperacion {

        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim().lowercase()

        if (nombreLimpio.isBlank()) {
            return ResultadoOperacion(false, "Ingresa tu nombre.")
        }

        if (!correoLimpio.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))) {
            return ResultadoOperacion(false, "Ingresa un correo válido.")
        }

        if (contrasena.length < 8) {
            return ResultadoOperacion(
                false,
                "La contraseña debe tener al menos 8 caracteres."
            )
        }

        val rolesPermitidos = setOf(
            "Estudiante",
            "Docente",
            "Administrador pedagógico (UTP)"
        )

        if (rol !in rolesPermitidos) {
            return ResultadoOperacion(false, "Selecciona un rol válido.")
        }

        if (usuarioDao.buscarPorCorreo(correoLimpio) != null) {
            return ResultadoOperacion(
                false,
                "Ya existe una cuenta con ese correo."
            )
        }

        val usuario = Usuario(
            nombre = nombreLimpio,
            correo = correoLimpio,
            contrasenaHash = PasswordHasher.generarHash(contrasena),
            rol = rol
        )

        return try {
            usuarioDao.insertar(usuario)
            ResultadoOperacion(
                true,
                "Cuenta creada correctamente."
            )
        } catch (_: SQLiteConstraintException) {
            ResultadoOperacion(
                false,
                "Ya existe una cuenta con ese correo."
            )
        }
    }

    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): ResultadoOperacion {

        val correoLimpio = correo.trim().lowercase()

        if (correoLimpio.isBlank() || contrasena.isBlank()) {
            return ResultadoOperacion(
                false,
                "Ingresa tu correo y contraseña."
            )
        }

        val usuario = usuarioDao.buscarPorCorreo(correoLimpio)

        if (
            usuario == null ||
            !PasswordHasher.verificar(
                contrasena,
                usuario.contrasenaHash
            )
        ) {
            return ResultadoOperacion(
                false,
                "Correo o contraseña incorrectos."
            )
        }

        return ResultadoOperacion(
            true,
            "Bienvenido, ${usuario.nombre}.",
            usuario
        )
    }
}
