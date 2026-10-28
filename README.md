
# MPAZ PRO 

## Descripción del proyecto

MPAZ PRO es una aplicación educativa desarrollada en Android Studio utilizando Kotlin y Jetpack Compose. Su objetivo es apoyar el aprendizaje mediante contenidos educativos, actividades interactivas y diferentes funcionalidades según el rol del usuario.

El proyecto fue desarrollado en equipo, trabajando en la construcción de la interfaz, la navegación entre pantallas y las funcionalidades correspondientes a los distintos perfiles.

##  Trabajo en equipo

El desarrollo se realizó de manera colaborativa entre dos integrantes, compartiendo las tareas de programación, integración y revisión de funcionalidades.

### Funcionalidades trabajadas

- Implementación y ajustes de la pantalla principal.
- Desarrollo de las pantallas de inicio de sesión y registro.
- Integración de la navegación entre las distintas secciones.
- Organización de los contenidos educativos por asignatura y unidad.
- Implementación de actividades con preguntas de selección múltiple.
- Conexión de las pantallas correspondientes a los roles Docente y Administrador pedagógico (UTP).
- Revisión y ajustes del código para integrar las funcionalidades existentes.

## Funcionalidades de la aplicación

### 1. Inicio de sesión y registro

La aplicación cuenta con pantallas para iniciar sesión y crear una cuenta. Durante el registro, el usuario puede seleccionar su rol dentro de la plataforma.

Roles considerados:
- **Estudiante:** accede a los contenidos y actividades educativas.
- **Docente:** accede al panel docente para gestionar las configuraciones educativas disponibles.
- **Administrador pedagógico (UTP):** accede al panel de revisión y seguimiento pedagógico.

### 2. Asignaturas educativas

El perfil de estudiante permite acceder a las siguientes asignaturas:

- Lenguaje.
- Matemática.
- Ciencias Naturales.
- Historia.

Cada asignatura contiene unidades con explicaciones y actividades para reforzar los contenidos estudiados.

### 3. Actividades interactivas

Las actividades incluyen preguntas de selección múltiple, comprobación de respuestas, retroalimentación y visualización de resultados.

Al finalizar una actividad, el estudiante puede revisar su puntaje, repasar el contenido o repetir la actividad.

### 4. Prueba 1 de Matemática

Se incorporó el acceso a la **Prueba 1**, ubicada en la asignatura Matemática, dentro de la unidad Operaciones básicas.

La aplicación permite responder las preguntas y visualizar el puntaje obtenido como referencia de práctica. La calificación definitiva se mantiene pendiente para una futura implementación.

### 5. Panel docente

Se integró el acceso a `PantallaDocente`, que permite trabajar con las configuraciones educativas implementadas para las asignaturas, incluyendo objetivos de aprendizaje, tipo de actividad, instrucciones y límite de intentos.

### 6. Panel de administración pedagógica (UTP)

Se integró el acceso a `PantallaUTP`, que contempla funcionalidades de revisión pedagógica, estados de revisión, observaciones, solicitudes de corrección, autorización y publicación según el flujo implementado.

##  Tecnologías utilizadas

- **Kotlin:** lenguaje de programación.
- **Android Studio:** entorno de desarrollo.
- **Jetpack Compose:** construcción de interfaces de usuario.
- **Material 3:** componentes visuales.
- **Room / DAO:** acceso y persistencia de datos locales, según la implementación del proyecto.
- **Git y GitHub:** control de versiones y trabajo colaborativo.

##  Estructura general

La aplicación se organiza en distintas pantallas y componentes:

- `MainActivity.kt`: actividad principal, navegación, contenido educativo y flujo de las pantallas.
- `PantallaDocente`: funcionalidades del perfil docente.
- `PantallaUTP`: funcionalidades del perfil de administración pedagógica.
- `MpazDatabase` y componentes relacionados: acceso a los datos de la aplicación.

##  Ejecución del proyecto

1. Clonar el repositorio desde GitHub.
2. Abrir el proyecto en Android Studio.
3. Esperar a que finalice la sincronización de Gradle.
4. Seleccionar un emulador Android o conectar un dispositivo compatible.
5. Ejecutar la aplicación desde Android Studio.

##  Colaboración

El trabajo se desarrolló entre dos compañeros, colaborando en la programación, integración de pantallas, revisión de funcionalidades y control de versiones mediante Git y GitHub.

El objetivo del trabajo colaborativo fue mantener las funcionalidades existentes y conectar los distintos módulos para ofrecer una experiencia de uso organizada según el perfil del usuario.

## Estado del proyecto

El proyecto cuenta con navegación por roles, contenidos educativos, actividades interactivas y acceso a los paneles docente y UTP. Algunas funcionalidades, como la gestión de la calificación definitiva de la Prueba 1, quedan pendientes de implementación.

---

**Proyecto académico: MPAZ PRO**  
**Desarrollo Android con Kotlin y Jetpack Compose**
