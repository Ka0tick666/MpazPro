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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.duoc.mpazpro.data.MpazDatabase
import com.duoc.mpazpro.data.Asignatura
import com.duoc.mpazpro.data.Unidad
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import com.duoc.mpazpro.data.Usuario
import com.duoc.mpazpro.data.UsuarioRepository
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
                                            mensaje =
                                                "No se pudo iniciar sesión. Inténtalo nuevamente."
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
                                            mensaje =
                                                "No se pudo crear la cuenta. Inténtalo nuevamente."
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
                color = if (
                    mensaje.contains("bienvenido", ignoreCase = true)
                ) {
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
                color = if (
                    mensaje.contains("correctamente", ignoreCase = true)
                ) {
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
                                "Para evitar estudiar acontecimientos"
                            ),
                            0,
                            "Las fuentes aportan evidencias para investigar el pasado."
                        )
                    )
                ),
                UnidadEducativa(
                    titulo = "Línea de tiempo",
                    descripcion = "Ordena acontecimientos cronológicamente.",
                    contenido = """
                        Una línea de tiempo representa acontecimientos en el orden en que ocurrieron.

                        La cronología permite ordenar hechos desde los más antiguos hasta los más recientes, o al revés.

                        Las fechas y los intervalos ayudan a comparar cuándo ocurrieron distintos acontecimientos y cuánto tiempo transcurrió entre ellos.
                    """.trimIndent(),
                    preguntas = listOf(
                        PreguntaEducativa(
                            "¿Qué representa una línea de tiempo?",
                            listOf(
                                "Acontecimientos ordenados temporalmente",
                                "Solo lugares geográficos",
                                "Únicamente personajes ficticios",
                                "Una lista sin orden"
                            ),
                            0,
                            "La línea de tiempo organiza acontecimientos según cuándo ocurrieron."
                        ),
                        PreguntaEducativa(
                            "¿Qué significa ordenar cronológicamente?",
                            listOf(
                                "Ordenar por tamaño",
                                "Ordenar según el tiempo",
                                "Ordenar por color",
                                "Ordenar al azar"
                            ),
                            1,
                            "La cronología organiza acontecimientos en relación con el tiempo."
                        ),
                        PreguntaEducativa(
                            "¿Qué información ayuda a ubicar un acontecimiento?",
                            listOf(
                                "Su fecha",
                                "Su color favorito",
                                "Su peso siempre",
                                "Su temperatura siempre"
                            ),
                            0,
                            "La fecha ayuda a identificar cuándo ocurrió un acontecimiento."
                        )
                    )
                )
            )
        )
    )
}

// -----------------------------------------------------
// PANEL DEL USUARIO Y NAVEGACIÓN EDUCATIVA
// -----------------------------------------------------

@Composable
fun PantallaPanel(
    modifier: Modifier = Modifier,
    usuario: Usuario,
    onCerrarSesion: () -> Unit
) {
    var pantallaEstudiante by remember { mutableStateOf("asignaturas") }
    var asignaturaSeleccionada by remember {
        mutableStateOf<AsignaturaEducativa?>(null)
    }
    var unidadSeleccionada by remember {
        mutableStateOf<UnidadEducativa?>(null)
    }

    var preguntaActual by remember { mutableIntStateOf(0) }
    var respuestaSeleccionada by remember { mutableStateOf<Int?>(null) }
    var puntaje by remember { mutableIntStateOf(0) }
    var respondidas by remember { mutableStateOf(false) }
    var esPrueba1 by remember { mutableStateOf(false) }

    val asignaturas = remember { obtenerAsignaturasEducativas() }

    // -----------------------------------------------------
    // ROOM COMPARTIDO + CARGA INICIAL DEL CONTENIDO EDUCATIVO
    // -----------------------------------------------------
    val contextRoom = LocalContext.current
    val dbRoom = remember(contextRoom) {
        MpazDatabase.obtenerInstancia(contextRoom)
    }
    val asignaturasRoom by dbRoom.asignaturaDao()
        .obtenerTodas()
        .collectAsState(initial = emptyList())
    val asignaturasPublicadasRoom by dbRoom.asignaturaDao()
        .obtenerPublicadas()
        .collectAsState(initial = emptyList())

    // Migra el contenido educativo clásico a Room una sola vez por apertura,
    // sin duplicar asignaturas ni unidades que ya existan.
    LaunchedEffect(dbRoom) {
        withContext(Dispatchers.IO) {
            val asignaturasIniciales = obtenerAsignaturasEducativas()
            val asignaturaDao = dbRoom.asignaturaDao()
            val unidadDao = dbRoom.unidadDao()

            for (asignaturaInicial in asignaturasIniciales) {
                var asignaturaRoom = asignaturaDao.obtenerTodas()
                    .first()
                    .firstOrNull {
                        it.nombre.trim().equals(
                            asignaturaInicial.nombre.trim(),
                            ignoreCase = true
                        )
                    }

                if (asignaturaRoom == null) {
                    val idNuevo = asignaturaDao.insertar(
                        Asignatura(
                            nombre = asignaturaInicial.nombre,
                            descripcion = asignaturaInicial.descripcion,
                            publicada = true
                        )
                    )
                    asignaturaRoom = asignaturaDao.obtenerTodas()
                        .first()
                        .firstOrNull { it.idAsignatura == idNuevo }
                }

                val asignaturaActual = asignaturaRoom ?: continue
                val titulosExistentes = unidadDao
                    .obtenerPorAsignatura(asignaturaActual.idAsignatura)
                    .first()
                    .map { it.titulo.trim().lowercase() }
                    .toMutableSet()

                for (unidadInicial in asignaturaInicial.unidades) {
                    val tituloNormalizado = unidadInicial.titulo.trim().lowercase()
                    if (tituloNormalizado !in titulosExistentes) {
                        unidadDao.insertar(
                            Unidad(
                                idAsignatura = asignaturaActual.idAsignatura,
                                titulo = unidadInicial.titulo,
                                descripcion = unidadInicial.descripcion,
                                contenido = unidadInicial.contenido,
                                publicada = asignaturaActual.publicada
                            )
                        )
                        titulosExistentes.add(tituloNormalizado)
                    }
                }
            }
        }
    }

    fun reiniciarActividad(esPrueba: Boolean = false) {
        preguntaActual = 0
        respuestaSeleccionada = null
        puntaje = 0
        respondidas = false
        esPrueba1 = esPrueba
        pantallaEstudiante = "actividad"
    }

    fun volverAAsignaturas() {
        pantallaEstudiante = "asignaturas"
        asignaturaSeleccionada = null
        unidadSeleccionada = null
        preguntaActual = 0
        respuestaSeleccionada = null
        puntaje = 0
        respondidas = false
        esPrueba1 = false
    }

    fun volverUnaPantalla() {
        when (pantallaEstudiante) {
            "unidades" -> volverAAsignaturas()

            "contenido" -> {
                pantallaEstudiante = "unidades"
                respuestaSeleccionada = null
                respondidas = false
            }

            "actividad" -> {
                pantallaEstudiante = "contenido"
                respuestaSeleccionada = null
                respondidas = false
            }

            "resultado" -> {
                pantallaEstudiante = "asignaturas"
                asignaturaSeleccionada = null
                unidadSeleccionada = null
                preguntaActual = 0
                respuestaSeleccionada = null
                puntaje = 0
                respondidas = false
                esPrueba1 = false
            }

            else -> volverAAsignaturas()
        }
    }

    // El estudiante siempre utiliza Room y solo recibe asignaturas/unidades publicadas.
    if (usuario.rol == "Estudiante") {
        PantallaEstudianteRoom(
            usuario = usuario,
            asignaturas = asignaturasPublicadasRoom,
            onCerrarSesion = onCerrarSesion
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pantallaEstudiante == "asignaturas") {
            Spacer(Modifier.height(12.dp))

            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = "Educación",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "¡Bienvenido a MPAZ PRO!",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            Text(usuario.nombre, fontSize = 18.sp)

            Text(
                usuario.correo,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Tu perfil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text("Rol: ${usuario.rol}")
                }
            }

            Spacer(Modifier.height(24.dp))

            if (usuario.rol == "Estudiante") {
                Text(
                    "Mis asignaturas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Selecciona una asignatura para comenzar.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                asignaturas.forEach { asignatura ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                asignatura.nombre,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))
                            Text(asignatura.descripcion)

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "${asignatura.unidades.size} unidades disponibles",
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    asignaturaSeleccionada = asignatura
                                    unidadSeleccionada = null
                                    preguntaActual = 0
                                    respuestaSeleccionada = null
                                    puntaje = 0
                                    respondidas = false
                                    esPrueba1 = false
                                    pantallaEstudiante = "unidades"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Ver unidades")
                            }
                        }
                    }
                }
            } else {
                // Conectar las pantallas funcionales existentes.
                when (usuario.rol) {
                    "Docente" -> {
                        PantallaDocente(
                            asignaturas = asignaturas
                        )
                    }

                    "Administrador pedagógico (UTP)" -> {
                        PantallaUTP(
                            asignaturas = asignaturas
                        )
                    }

                    else -> {
                        Text(
                            "Rol no reconocido.",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = { volverUnaPantalla() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when (pantallaEstudiante) {
                        "unidades" -> "← Volver a las asignaturas"
                        "contenido" -> "← Volver a las unidades"
                        "actividad" -> "← Volver al contenido"
                        "resultado" -> "← Volver a las asignaturas"
                        else -> "← Volver"
                    }
                )
            }

            Spacer(Modifier.height(20.dp))

            when (pantallaEstudiante) {
                "unidades" -> {
                    val asignatura = asignaturaSeleccionada

                    if (asignatura != null) {
                        Text(
                            asignatura.nombre,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Selecciona una unidad para aprender.",
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(16.dp))

                        asignatura.unidades.forEachIndexed { indice, unidad ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Unidad ${indice + 1}",
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        unidad.titulo,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(Modifier.height(6.dp))
                                    Text(unidad.descripcion)

                                    Spacer(Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            unidadSeleccionada = unidad
                                            preguntaActual = 0
                                            respuestaSeleccionada = null
                                            puntaje = 0
                                            respondidas = false
                                            esPrueba1 = false
                                            pantallaEstudiante = "contenido"
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Estudiar unidad")
                                    }
                                }
                            }

                            // Prueba 1 exclusiva de Matemática / Operaciones básicas.
                            if (
                                usuario.rol == "Estudiante" &&
                                asignatura.nombre == "Matemática" &&
                                unidad.titulo == "Operaciones básicas"
                            ) {
                                Spacer(Modifier.height(4.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            "Prueba 1",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(Modifier.height(6.dp))

                                        Text(
                                            "Realiza la prueba de operaciones básicas. La calificación definitiva estará disponible en una futura actualización."
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        Button(
                                            onClick = {
                                                unidadSeleccionada = unidad
                                                reiniciarActividad(esPrueba = true)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Comenzar Prueba 1")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "contenido" -> {
                    val unidad = unidadSeleccionada

                    if (unidad != null) {
                        Text(
                            unidad.titulo,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(20.dp))

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Text(
                                    "Aprendamos",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    unidad.contenido,
                                    fontSize = 16.sp,
                                    lineHeight = 25.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "¿Listo para practicar lo aprendido?",
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = { reiniciarActividad() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Comenzar actividad")
                        }
                    }
                }

                "actividad" -> {
                    val unidad = unidadSeleccionada

                    if (unidad != null && unidad.preguntas.isNotEmpty()) {
                        val pregunta = unidad.preguntas[preguntaActual]

                        Text(
                            if (esPrueba1) "Prueba 1: ${unidad.titulo}"
                            else "Actividad: ${unidad.titulo}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Pregunta ${preguntaActual + 1} de ${unidad.preguntas.size}",
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(Modifier.height(16.dp))

                        LinearProgressIndicator(
                            progress = {
                                (preguntaActual + 1).toFloat() /
                                        unidad.preguntas.size
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(24.dp))

                        Text(
                            pregunta.enunciado,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(16.dp))

                        pregunta.opciones.forEachIndexed { indice, opcion ->
                            OutlinedButton(
                                onClick = {
                                    if (!respondidas) {
                                        respuestaSeleccionada = indice
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                enabled = !respondidas,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor =
                                        if (respuestaSeleccionada == indice) {
                                            MaterialTheme.colorScheme.secondaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                )
                            ) {
                                Text(
                                    opcion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                )
                            }
                        }

                        if (respondidas) {
                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = if (
                                    respuestaSeleccionada == pregunta.indiceCorrecto
                                ) {
                                    "¡Correcto! ${pregunta.explicacion}"
                                } else {
                                    "La respuesta correcta es: " +
                                            "${pregunta.opciones[pregunta.indiceCorrecto]}. " +
                                            pregunta.explicacion
                                },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (!respondidas) {
                                    val seleccion = respuestaSeleccionada

                                    if (seleccion != null) {
                                        if (seleccion == pregunta.indiceCorrecto) {
                                            puntaje++
                                        }

                                        respondidas = true
                                    }
                                } else {
                                    if (preguntaActual < unidad.preguntas.lastIndex) {
                                        preguntaActual++
                                        respuestaSeleccionada = null
                                        respondidas = false
                                    } else {
                                        pantallaEstudiante = "resultado"
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = respondidas || respuestaSeleccionada != null
                        ) {
                            Text(
                                when {
                                    !respondidas -> "Comprobar respuesta"
                                    preguntaActual < unidad.preguntas.lastIndex ->
                                        "Siguiente pregunta"
                                    else -> "Ver resultado"
                                }
                            )
                        }
                    } else {
                        Text("No hay preguntas disponibles para esta unidad.")
                    }
                }

                "resultado" -> {
                    val unidad = unidadSeleccionada
                    val total = unidad?.preguntas?.size ?: 0
                    val porcentaje = if (total > 0) {
                        (puntaje * 100) / total
                    } else {
                        0
                    }

                    Spacer(Modifier.height(8.dp))

                    Icon(
                        imageVector = Icons.Filled.Book,
                        contentDescription = "Actividad completada",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = if (esPrueba1) {
                            "¡Prueba 1 completada!"
                        } else {
                            "¡Actividad completada!"
                        },
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = unidad?.titulo ?: "Actividad",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$puntaje de $total",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text("Respuestas correctas")

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = "$porcentaje%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                if (esPrueba1) "Puntaje de práctica"
                                else "Puntaje obtenido"
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    if (esPrueba1) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Calificación pendiente",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    "Tu calificación definitiva aún no está disponible. Esta funcionalidad estará disponible en una futura actualización.",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Text(
                            text = when {
                                porcentaje == 100 ->
                                    "¡Excelente! Respondiste todas las preguntas correctamente."
                                porcentaje >= 60 ->
                                    "¡Buen trabajo! Sigue practicando para mejorar tu resultado."
                                else ->
                                    "¡Sigue aprendiendo! Puedes repasar el contenido e intentarlo nuevamente."
                            },
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            reiniciarActividad(esPrueba = esPrueba1)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Repetir actividad")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            pantallaEstudiante = "contenido"
                            preguntaActual = 0
                            respuestaSeleccionada = null
                            puntaje = 0
                            respondidas = false
                            esPrueba1 = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Repasar contenido")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            pantallaEstudiante = "unidades"
                            preguntaActual = 0
                            respuestaSeleccionada = null
                            puntaje = 0
                            respondidas = false
                            esPrueba1 = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a las unidades")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { volverAAsignaturas() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver a las asignaturas")
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(16.dp))
    }
}


// -----------------------------------------------------
// PANEL ESTUDIANTE CON DATOS COMPARTIDOS DE ROOM
// -----------------------------------------------------

@Composable
private fun PantallaEstudianteRoom(
    usuario: Usuario,
    asignaturas: List<Asignatura>,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val db = remember(context) { MpazDatabase.obtenerInstancia(context) }

    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var unidadSeleccionada by remember { mutableStateOf<Unidad?>(null) }
    var vista by remember { mutableStateOf("asignaturas") }

    val unidadesFlow = remember(asignaturaSeleccionada?.idAsignatura) {
        val id = asignaturaSeleccionada?.idAsignatura
        if (id == null) flowOf<List<Unidad>>(emptyList()) else db.unidadDao().obtenerPublicadas(id)
    }
    val unidades by unidadesFlow.collectAsState(initial = emptyList())

    val contenidoClasico = remember { obtenerAsignaturasEducativas() }
    val unidadClasica = remember(unidadSeleccionada?.titulo, asignaturaSeleccionada?.nombre) {
        val nombreAsignatura = asignaturaSeleccionada?.nombre
        val tituloUnidad = unidadSeleccionada?.titulo
        contenidoClasico.firstOrNull { it.nombre.equals(nombreAsignatura, true) }
            ?.unidades?.firstOrNull { it.titulo.equals(tituloUnidad, true) }
    }

    var indicePregunta by remember { mutableIntStateOf(0) }
    var respuestaElegida by remember { mutableStateOf<Int?>(null) }
    var respondida by remember { mutableStateOf(false) }
    var puntaje by remember { mutableIntStateOf(0) }
    var esPrueba1 by remember { mutableStateOf(false) }

    fun iniciarPractica(prueba: Boolean) {
        indicePregunta = 0
        respuestaElegida = null
        respondida = false
        puntaje = 0
        esPrueba1 = prueba
        vista = "actividad"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (vista != "asignaturas") {
            OutlinedButton(
                onClick = {
                    when (vista) {
                        "unidades" -> { vista = "asignaturas"; asignaturaSeleccionada = null }
                        "contenido" -> vista = "unidades"
                        "actividad", "resultado" -> vista = if (esPrueba1 || unidadSeleccionada != null) "contenido" else "unidades"
                        else -> vista = "asignaturas"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("← Volver") }
            Spacer(Modifier.height(16.dp))
        }

        when (vista) {
            "asignaturas" -> {
                Icon(Icons.Filled.Book, contentDescription = "Educación", modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                Text("¡Bienvenido a MPAZ PRO!", fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text(usuario.nombre, fontSize = 18.sp)
                Text(usuario.correo, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(20.dp))
                Text("Mis asignaturas", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                if (asignaturas.isEmpty()) {
                    Text("Por ahora no hay asignaturas publicadas por UTP.", textAlign = TextAlign.Center)
                } else {
                    asignaturas.forEach { asignatura ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(asignatura.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(asignatura.descripcion.ifBlank { "Sin descripción." })
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { asignaturaSeleccionada = asignatura; unidadSeleccionada = null; vista = "unidades" },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Ver unidades") }
                            }
                        }
                    }
                }
            }

            "unidades" -> {
                Text(asignaturaSeleccionada?.nombre ?: "Asignatura", fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                if (unidades.isEmpty()) {
                    Text("Esta asignatura todavía no tiene unidades publicadas.", textAlign = TextAlign.Center)
                } else {
                    unidades.forEachIndexed { index, unidad ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Unidad ${index + 1}", color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(4.dp))
                                Text(unidad.titulo, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text(unidad.descripcion.ifBlank { "Sin descripción." })
                                Spacer(Modifier.height(10.dp))
                                Button(onClick = { unidadSeleccionada = unidad; vista = "contenido" }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Estudiar unidad")
                                }
                                if (asignaturaSeleccionada?.nombre.equals("Matemática", true) && unidad.titulo.equals("Operaciones básicas", true)) {
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = { unidadSeleccionada = unidad; iniciarPractica(true) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = contenidoClasico.firstOrNull { it.nombre.equals("Matemática", true) }?.unidades?.any { it.titulo.equals("Operaciones básicas", true) && it.preguntas.isNotEmpty() } == true
                                    ) { Text("Comenzar Prueba 1") }
                                    Text("La calificación definitiva estará disponible en una futura actualización.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            "contenido" -> {
                val unidad = unidadSeleccionada
                Text(unidad?.titulo ?: "Unidad", fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Aprendamos", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))
                        Text(unidad?.contenido?.ifBlank { "Todavía no se ha agregado contenido." } ?: "Sin contenido.", fontSize = 16.sp, lineHeight = 25.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val tienePreguntas = unidadClasica?.preguntas?.isNotEmpty() == true
                if (tienePreguntas) {
                    Button(onClick = { iniciarPractica(false) }, modifier = Modifier.fillMaxWidth()) { Text("Comenzar actividad") }
                } else {
                    Text("Esta unidad todavía no tiene una actividad de preguntas asociada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            "actividad" -> {
                val preguntas = unidadClasica?.preguntas.orEmpty()
                if (preguntas.isEmpty()) {
                    Text("No hay preguntas disponibles para esta unidad.")
                } else {
                    val pregunta = preguntas[indicePregunta]
                    Text(if (esPrueba1) "Prueba 1: ${unidadSeleccionada?.titulo}" else "Actividad: ${unidadSeleccionada?.titulo}", fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("Pregunta ${indicePregunta + 1} de ${preguntas.size}", color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { (indicePregunta + 1).toFloat() / preguntas.size }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(20.dp))
                    Text(pregunta.enunciado, fontSize = 19.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                    pregunta.opciones.forEachIndexed { index, opcion ->
                        OutlinedButton(
                            onClick = { if (!respondida) respuestaElegida = index },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            enabled = !respondida,
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (respuestaElegida == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface)
                        ) { Text(opcion, modifier = Modifier.fillMaxWidth()) }
                    }
                    if (respondida) {
                        Spacer(Modifier.height(10.dp))
                        Text(if (respuestaElegida == pregunta.indiceCorrecto) "¡Correcto! ${pregunta.explicacion}" else "La respuesta correcta es: ${pregunta.opciones[pregunta.indiceCorrecto]}. ${pregunta.explicacion}", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (!respondida) {
                                if (respuestaElegida == pregunta.indiceCorrecto) puntaje++
                                respondida = true
                            } else if (indicePregunta < preguntas.lastIndex) {
                                indicePregunta++
                                respuestaElegida = null
                                respondida = false
                            } else {
                                vista = "resultado"
                            }
                        },
                        enabled = respondida || respuestaElegida != null,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (!respondida) "Comprobar respuesta" else if (indicePregunta < preguntas.lastIndex) "Siguiente pregunta" else "Ver resultado") }
                }
            }

            "resultado" -> {
                val total = unidadClasica?.preguntas?.size ?: 0
                val porcentaje = if (total > 0) puntaje * 100 / total else 0
                Icon(Icons.Filled.Book, contentDescription = "Actividad completada", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(if (esPrueba1) "¡Prueba 1 completada!" else "¡Actividad completada!", fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$puntaje de $total", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                        Text("Respuestas correctas")
                        Spacer(Modifier.height(8.dp))
                        Text("$porcentaje%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(if (esPrueba1) "Puntaje de práctica" else "Puntaje obtenido")
                    }
                }
                if (esPrueba1) {
                    Spacer(Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Calificación pendiente", fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text("Tu calificación definitiva aún no está disponible. Esta funcionalidad estará disponible en una futura actualización.", textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                    Text(if (porcentaje == 100) "¡Excelente! Respondiste todas las preguntas correctamente." else if (porcentaje >= 60) "¡Buen trabajo! Sigue practicando para mejorar tu resultado." else "¡Sigue aprendiendo! Puedes repasar el contenido e intentarlo nuevamente.", textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(20.dp))
                Button(onClick = { iniciarPractica(esPrueba1) }, modifier = Modifier.fillMaxWidth()) { Text("Repetir actividad") }
                OutlinedButton(onClick = { vista = "contenido"; indicePregunta = 0; respuestaElegida = null; respondida = false; puntaje = 0; esPrueba1 = false }, modifier = Modifier.fillMaxWidth()) { Text("Repasar contenido") }
                OutlinedButton(onClick = { vista = "unidades"; indicePregunta = 0; respuestaElegida = null; respondida = false; puntaje = 0; esPrueba1 = false }, modifier = Modifier.fillMaxWidth()) { Text("Volver a las unidades") }
            }
        }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
        Spacer(Modifier.height(12.dp))
    }
}
