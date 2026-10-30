package com.duoc.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.Usuario
import com.duoc.mpazpro.data.UsuarioRepository
import com.duoc.mpazpro.ui.screens.PantallaActividadEstudiante
import com.duoc.mpazpro.ui.theme.MpazProTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MpazProTheme {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()

                val repository = remember {
                    UsuarioRepository(
                        MpazDatabase.obtenerInstancia(context).usuarioDao()
                    )
                }

                var pantalla by remember { mutableStateOf("inicio") }
                var usuarioActual by remember { mutableStateOf<Usuario?>(null) }
                var mensaje by remember { mutableStateOf("") }
                var cargando by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (pantalla) {
                        "inicio" -> PantallaInicio(
                            onIniciarSesion = {
                                mensaje = ""
                                pantalla = "login"
                            },
                            onRegistrarse = {
                                mensaje = ""
                                pantalla = "registro"
                            }
                        )

                        "login" -> PantallaLogin(
                            mensaje = mensaje,
                            cargando = cargando,
                            onVolver = {
                                mensaje = ""
                                pantalla = "inicio"
                            },
                            onCrearCuenta = {
                                mensaje = ""
                                pantalla = "registro"
                            },
                            onIngresar = { correo, contrasena ->
                                if (!cargando) {
                                    scope.launch {
                                        cargando = true
                                        mensaje = ""

                                        try {
                                            val resultado = withContext(Dispatchers.IO) {
                                                repository.iniciarSesion(
                                                    correo.trim(),
                                                    contrasena
                                                )
                                            }

                                            mensaje = resultado.mensaje

                                            if (
                                                resultado.exito &&
                                                resultado.usuario != null
                                            ) {
                                                usuarioActual = resultado.usuario
                                                pantalla = "panel"
                                            }
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo iniciar sesión. Inténtalo nuevamente."
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                }
                            }
                        )

                        "registro" -> PantallaRegistro(
                            mensaje = mensaje,
                            cargando = cargando,
                            onVolver = {
                                mensaje = ""
                                pantalla = "inicio"
                            },
                            onIniciarSesion = {
                                mensaje = ""
                                pantalla = "login"
                            },
                            onRegistrar = { nombre, correo, contrasena, rol ->
                                if (!cargando) {
                                    scope.launch {
                                        cargando = true
                                        mensaje = ""

                                        try {
                                            val resultado = withContext(Dispatchers.IO) {
                                                repository.registrar(
                                                    nombre.trim(),
                                                    correo.trim(),
                                                    contrasena,
                                                    rol
                                                )
                                            }

                                            mensaje = resultado.mensaje

                                            if (resultado.exito) {
                                                pantalla = "login"
                                            }
                                        } catch (e: Exception) {
                                            mensaje = "No se pudo crear la cuenta. Inténtalo nuevamente."
                                        } finally {
                                            cargando = false
                                        }
                                    }
                                }
                            }
                        )

                        "panel" -> {
                            val usuario = usuarioActual

                            if (usuario != null) {
                                PantallaPanel(
                                    usuario = usuario,
                                    onCerrarSesion = {
                                        usuarioActual = null
                                        mensaje = ""
                                        pantalla = "inicio"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------
// PANTALLA DE INICIO
// -----------------------------------------------------

@Composable
fun PantallaInicio(
    modifier: Modifier = Modifier,
    onIniciarSesion: () -> Unit,
    onRegistrarse: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Book,
            contentDescription = "Libro educativo",
            modifier = Modifier.size(90.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "MPAZ PRO",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Aprender es una aventura",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        Button(
            onClick = onIniciarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar sesión")
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegistrarse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear una cuenta")
        }
    }
}

// -----------------------------------------------------
// PANTALLA DE INICIO DE SESIÓN
// -----------------------------------------------------

@Composable
fun PantallaLogin(
    modifier: Modifier = Modifier,
    mensaje: String,
    cargando: Boolean,
    onVolver: () -> Unit,
    onCrearCuenta: () -> Unit,
    onIngresar: (String, String) -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Iniciar sesión",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        Spacer(Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (mensaje.contains("bienvenido", ignoreCase = true)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = { onIngresar(correo, contrasena) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ingresar")
            }
        }

        TextButton(
            onClick = onCrearCuenta,
            enabled = !cargando
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }

        TextButton(
            onClick = onVolver,
            enabled = !cargando
        ) {
            Text("Volver al inicio")
        }
    }
}

// -----------------------------------------------------
// PANTALLA DE REGISTRO
// -----------------------------------------------------

@Composable
fun PantallaRegistro(
    modifier: Modifier = Modifier,
    mensaje: String,
    cargando: Boolean,
    onVolver: () -> Unit,
    onIniciarSesion: () -> Unit,
    onRegistrar: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    var rol by remember { mutableStateOf("Estudiante") }

    val roles = listOf(
        "Estudiante",
        "Docente",
        "Administrador pedagógico (UTP)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))

        Text(
            "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña (mínimo 8 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "Selecciona tu rol",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        roles.forEach { opcion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = rol == opcion,
                    onClick = { rol = opcion },
                    enabled = !cargando
                )

                Text(
                    text = opcion,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                color = if (mensaje.contains("correctamente", ignoreCase = true)) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                onRegistrar(nombre, correo, contrasena, rol)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Registrarme")
            }
        }

        TextButton(
            onClick = onIniciarSesion,
            enabled = !cargando
        ) {
            Text("Ya tengo una cuenta")
        }

        TextButton(
            onClick = onVolver,
            enabled = !cargando
        ) {
            Text("Volver al inicio")
        }

        Spacer(Modifier.height(20.dp))
    }
}

// -----------------------------------------------------
// MODELOS EDUCATIVOS
// -----------------------------------------------------

data class PreguntaEducativa(
    val enunciado: String,
    val opciones: List<String>,
    val indiceCorrecto: Int,
    val explicacion: String
)

data class UnidadEducativa(
    val titulo: String,
    val descripcion: String,
    val contenido: String,
    val preguntas: List<PreguntaEducativa>
)

data class AsignaturaEducativa(
    val nombre: String,
    val descripcion: String,
    val unidades: List<UnidadEducativa>
)

// -----------------------------------------------------
// CONTENIDO EDUCATIVO
// -----------------------------------------------------

fun obtenerAsignaturasEducativas(): List<AsignaturaEducativa> {
    return listOf(
        AsignaturaEducativa(
            nombre = "Lenguaje",
            descripcion = "Lectura, comprensión y comunicación.",
            unidades = listOf(
                UnidadEducativa(
                    titulo = "Comprensión lectora",
                    descripcion = "Identifica las ideas principales de un texto.",
                    contenido = """
                        Comprender un texto significa reconocer qué nos quiere comunicar.

                        La idea principal expresa el mensaje más importante. Las ideas secundarias entregan detalles, ejemplos o explicaciones que ayudan a entenderlo.

                        Para comprender mejor, lee con atención, identifica el tema y pregúntate qué quiere explicar el autor.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Qué expresa la idea principal?",
                            listOf(
                                "El mensaje más importante",
                                "Un detalle sin importancia",
                                "El nombre del autor",
                                "La cantidad de párrafos"
                            ),
                            0,
                            "La idea principal resume el mensaje central."
                        ),
                        PreguntaEducativa(
                            "¿Para qué sirven las ideas secundarias?",
                            listOf(
                                "Para reemplazar todo el texto",
                                "Para apoyar la idea principal",
                                "Para indicar siempre el final",
                                "Para contar las palabras"
                            ),
                            1,
                            "Las ideas secundarias amplían la información principal."
                        ),
                        PreguntaEducativa(
                            "¿Qué conviene hacer al leer un texto?",
                            listOf(
                                "Leer sin prestar atención",
                                "Mirar solo el título",
                                "Identificar el tema y el mensaje",
                                "Memorizar cada letra"
                            ),
                            2,
                            "Identificar el tema facilita la comprensión."
                        )
                    )
                ),
                UnidadEducativa(
                    titulo = "Tipos de textos",
                    descripcion = "Conoce los textos narrativos, informativos e instructivos.",
                    contenido = """
                        Los textos pueden tener distintos propósitos.

                        Un texto narrativo relata hechos o acontecimientos. Un texto informativo comunica datos o explica un tema. Un texto instructivo presenta pasos para realizar una tarea.

                        Reconocer el propósito de un texto permite comprender mejor su estructura.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Cuál es el propósito de un texto narrativo?",
                            listOf(
                                "Relatar acontecimientos",
                                "Dar solamente instrucciones",
                                "Mostrar únicamente números",
                                "Presentar una lista de compras"
                            ),
                            0,
                            "Los textos narrativos cuentan acontecimientos."
                        ),
                        PreguntaEducativa(
                            "¿Qué hace un texto instructivo?",
                            listOf(
                                "Cuenta una leyenda necesariamente",
                                "Presenta pasos para realizar algo",
                                "Describe siempre un paisaje",
                                "Expresa únicamente sentimientos"
                            ),
                            1,
                            "Un texto instructivo explica cómo realizar una tarea."
                        ),
                        PreguntaEducativa(
                            "¿Qué busca un texto informativo?",
                            listOf(
                                "Ocultar información",
                                "Crear personajes siempre",
                                "Comunicar datos o explicar un tema",
                                "Dar órdenes sin contexto"
                            ),
                            2,
                            "Un texto informativo comunica información."
                        )
                    )
                )
            )
        ),
        AsignaturaEducativa(
            nombre = "Matemática",
            descripcion = "Números, operaciones y razonamiento.",
            unidades = listOf(
                UnidadEducativa(
                    titulo = "Operaciones básicas",
                    descripcion = "Practica la suma, resta, multiplicación y división.",
                    contenido = """
                        La suma permite reunir cantidades. La resta permite calcular una diferencia.

                        La multiplicación representa una suma repetida de cantidades iguales. La división permite repartir una cantidad en partes iguales.

                        Puedes comprobar muchos resultados utilizando la operación inversa.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Cuánto es 8 + 7?",
                            listOf("13", "14", "15", "16"),
                            2,
                            "Ocho más siete es quince."
                        ),
                        PreguntaEducativa(
                            "¿Cuánto es 6 × 4?",
                            listOf("10", "24", "20", "28"),
                            1,
                            "Seis grupos de cuatro suman veinticuatro."
                        ),
                        PreguntaEducativa(
                            "¿Cuánto es 20 ÷ 5?",
                            listOf("2", "3", "4", "5"),
                            2,
                            "Veinte dividido en cinco partes es cuatro."
                        )
                    )
                ),
                UnidadEducativa(
                    titulo = "Fracciones",
                    descripcion = "Aprende a interpretar partes de un entero.",
                    contenido = """
                        Una fracción representa partes de un entero dividido en partes iguales.

                        El numerador, ubicado arriba, indica cuántas partes se consideran. El denominador, ubicado abajo, indica en cuántas partes iguales se divide el entero.

                        Por ejemplo, 1/2 representa una de dos partes iguales.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "En la fracción 3/4, ¿cuál es el numerador?",
                            listOf("4", "3", "7", "1"),
                            1,
                            "El numerador es el número que aparece arriba."
                        ),
                        PreguntaEducativa(
                            "¿Qué representa 1/2?",
                            listOf(
                                "Dos enteros completos",
                                "Una de dos partes iguales",
                                "Tres partes iguales",
                                "La mitad de dos enteros siempre"
                            ),
                            1,
                            "Un medio representa una de dos partes iguales."
                        ),
                        PreguntaEducativa(
                            "¿Cuál es el denominador de 2/5?",
                            listOf("2", "3", "5", "7"),
                            2,
                            "El denominador es el número que aparece abajo."
                        )
                    )
                )
            )
        ),
        AsignaturaEducativa(
            nombre = "Ciencias Naturales",
            descripcion = "Explora los seres vivos y el entorno.",
            unidades = listOf(
                UnidadEducativa(
                    titulo = "Los seres vivos",
                    descripcion = "Reconoce características de los organismos.",
                    contenido = """
                        Los seres vivos realizan procesos como la nutrición, el crecimiento y la reproducción.

                        Los animales, las plantas, los hongos y los microorganismos son ejemplos de seres vivos.

                        Los seres vivos interactúan con su ambiente y necesitan condiciones adecuadas para vivir.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Cuál es un ejemplo de ser vivo?",
                            listOf("Una piedra", "Una planta", "Un vaso", "Una silla"),
                            1,
                            "Las plantas son organismos vivos."
                        ),
                        PreguntaEducativa(
                            "¿Qué proceso realizan los seres vivos?",
                            listOf(
                                "Crecimiento",
                                "Fabricación de plástico",
                                "Oxidación de metales únicamente",
                                "Funcionamiento sin energía"
                            ),
                            0,
                            "El crecimiento es una característica de los seres vivos."
                        ),
                        PreguntaEducativa(
                            "¿Con qué interactúan los seres vivos?",
                            listOf(
                                "Solo con objetos artificiales",
                                "Con su ambiente",
                                "Con ningún elemento",
                                "Únicamente con el agua"
                            ),
                            1,
                            "Los seres vivos se relacionan con su entorno."
                        )
                    )
                ),
                UnidadEducativa(
                    titulo = "Ecosistemas",
                    descripcion = "Comprende la relación entre organismos y ambiente.",
                    contenido = """
                        Un ecosistema incluye los seres vivos de un lugar y los elementos no vivos con los que interactúan.

                        Los factores bióticos son los seres vivos. Los factores abióticos incluyen elementos como el agua, la luz, el aire y la temperatura.

                        Los organismos se relacionan entre sí y con las condiciones del ambiente.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Cuál es un factor biótico?",
                            listOf("La luz solar", "El agua", "Un árbol", "La temperatura"),
                            2,
                            "Un árbol es un ser vivo."
                        ),
                        PreguntaEducativa(
                            "¿Cuál es un factor abiótico?",
                            listOf("Un ave", "Un hongo", "Una planta", "El agua"),
                            3,
                            "El agua es un componente no vivo del ecosistema."
                        ),
                        PreguntaEducativa(
                            "¿Qué incluye un ecosistema?",
                            listOf(
                                "Solo animales",
                                "Seres vivos y elementos no vivos relacionados",
                                "Solo plantas",
                                "Únicamente rocas"
                            ),
                            1,
                            "Un ecosistema incluye organismos y componentes de su entorno."
                        )
                    )
                )
            )
        ),
        AsignaturaEducativa(
            nombre = "Historia",
            descripcion = "Comprende el pasado y las sociedades.",
            unidades = listOf(
                UnidadEducativa(
                    titulo = "Fuentes históricas",
                    descripcion = "Descubre cómo conocemos el pasado.",
                    contenido = """
                        Las fuentes históricas permiten investigar acontecimientos y formas de vida del pasado.

                        Las fuentes materiales incluyen objetos y construcciones. Las fuentes escritas incluyen cartas, documentos y registros. También existen fuentes orales, como testimonios y relatos transmitidos por personas.

                        Los historiadores comparan distintas fuentes para construir explicaciones fundamentadas.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Cuál es un ejemplo de fuente escrita?",
                            listOf(
                                "Una carta antigua",
                                "Una montaña",
                                "Una nube",
                                "Un río"
                            ),
                            0,
                            "Una carta contiene información escrita sobre el pasado."
                        ),
                        PreguntaEducativa(
                            "¿Qué es una fuente oral?",
                            listOf(
                                "Una herramienta de piedra",
                                "Un testimonio contado por una persona",
                                "Un edificio vacío",
                                "Un mapa sin información"
                            ),
                            1,
                            "Los testimonios de personas son fuentes orales."
                        ),
                        PreguntaEducativa(
                            "¿Para qué sirven las fuentes históricas?",
                            listOf(
                                "Para investigar el pasado",
                                "Para predecir exactamente el futuro",
                                "Para reemplazar toda investigación",
                                "Para ignorar la historia"
                            ),
                            0,
                            "Permiten comprender acontecimientos pasados."
                        )
                    )
                )
            )
        )
    )
}

// -----------------------------------------------------
// PANTALLA PANEL (ENRUTADOR POR ROL)
// -----------------------------------------------------

@Composable
fun PantallaPanel(
    usuario: Usuario,
    onCerrarSesion: () -> Unit
) {
    val asignaturasEducativas = remember { obtenerAsignaturasEducativas() }
    val rolNormalizado = usuario.rol.trim().uppercase()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hola, ${usuario.nombre}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            OutlinedButton(onClick = onCerrarSesion) {
                Text("Cerrar sesión")
            }
        }

        HorizontalDivider()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                rolNormalizado.contains("ESTUDIANTE") -> {
                    PantallaActividadEstudiante(
                        asignaturas = asignaturasEducativas
                    )
                }

                rolNormalizado.contains("DOCENTE") -> {
                    PantallaDocente(
                        asignaturas = asignaturasEducativas
                    )
                }

                rolNormalizado.contains("UTP") || rolNormalizado.contains("ADMINISTRADOR") -> {
                    PantallaUTP(
                        asignaturas = asignaturasEducativas
                    )
                }

                else -> {
                    Text(
                        text = "Rol no reconocido ($rolNormalizado). Contacta al administrador.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}