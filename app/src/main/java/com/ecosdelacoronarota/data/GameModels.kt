package com.ecosdelacoronarota.data

import kotlinx.serialization.Serializable

/**
 * Estado vital de cada investigador o personaje dentro de la partida.
 */
@Serializable
enum class EstadoJugador {
    ACTIVO,
    INCONSCIENTE,
    MUERTO
}

/**
 * Franjas temporales dentro de un día de campaña.
 */
@Serializable
enum class MomentoDia(val etiqueta: String) {
    MANANA("Mañana"),
    TARDE("Tarde"),
    NOCHE("Noche"),
    VELADA("Velada")
}

/**
 * Tipos de acciones válidas que un jugador puede intentar en su turno.
 */
@Serializable
enum class TipoAccion(val etiqueta: String) {
    MOVER("Mover"),
    HABLAR("Hablar"),
    LEER("Leer"),
    EJERCITARSE("Ejercitarse"),
    USAR_OBJETO("Usar objeto"),
    ATACAR("Atacar"),
    EXAMINAR("Examinar"),
    ESPERAR("Esperar")
}

/**
 * Modelo principal de un jugador.
 * Regla de inventario: máximo 3 objetos útiles; "Llave" y "Diario" no cuentan en el límite.
 */
@Serializable
data class Jugador(
    val id: String,
    val nombre: String,
    val habitacion: String,
    val hpActual: Int,
    val hpMaximo: Int = 20,
    val estado: EstadoJugador = EstadoJugador.ACTIVO,
    val inventario: List<String> = emptyList(),
    val exp: Map<String, Int> = emptyMap(),
    val ubicacionVisible: Boolean = true,
    val notas: String = ""
) {
    /**
     * Calcula cuántos objetos útiles ocupa el jugador en sus 3 espacios disponibles.
     * Cualquier llave de dormitorio y diario personal están exentos del conteo.
     */
    fun contarObjetosUtiles(): Int {
        return inventario.count { item ->
            val normalizado = item.trim().lowercase()
            !normalizado.startsWith("llave") && !normalizado.startsWith("diario")
        }
    }
}

/**
 * Representa el estado temporal actual de la partida (Día, momento, turno global 1..15 y acciones).
 */
@Serializable
data class Turno(
    val dia: Int = 1,
    val momento: MomentoDia = MomentoDia.MANANA,
    val turnoGlobal: Int = 1,
    val turnoMaximo: Int = 15,
    val accionesRestantes: Int = 3
) {
    /**
     * Devuelve el texto formateado para la cabecera superior:
     * Ejemplo: "Día 1 — Tarde — Turno 7/15"
     */
    fun textoIndicador(): String = "Día $dia — ${momento.etiqueta} — Turno $turnoGlobal/$turnoMaximo"
}

/**
 * Registro estructurado de una acción validada por el Panel del GM.
 */
@Serializable
data class Accion(
    val id: String,
    val jugadorId: String,
    val jugadorNombre: String,
    val tipo: TipoAccion,
    val descripcion: String,
    val habitacionOrigen: String,
    val habitacionDestino: String? = null,
    val turnoGlobal: Int,
    val resumenEfectos: String = ""
)

/**
 * Página individual dentro de un capítulo de la Biblioteca.
 */
@Serializable
data class Pagina(
    val titulo: String,
    val texto: String
)

/**
 * Capítulo perteneciente a un tomo arcano o técnico.
 */
@Serializable
data class Capitulo(
    val numero: Int,
    val titulo: String,
    val paginas: List<Pagina>
)

/**
 * Libro completo de la Biblioteca de la mansión.
 * Incluye propiedad calculada de páginas planas para cumplir el acceso directo si se requiere.
 */
@Serializable
data class Libro(
    val id: String,
    val titulo: String,
    val categoria: String,
    val capitulos: List<Capitulo>,
    val paginas: List<Pagina> = emptyList()
) {
    fun todasLasPaginas(): List<Pagina> =
        if (paginas.isNotEmpty()) paginas else capitulos.flatMap { it.paginas }
}

/**
 * Representación de una estancia del mapa con salidas conectadas, objetos y jugadores presentes.
 */
@Serializable
data class Habitacion(
    val id: String,
    val nombre: String,
    val zona: String,
    val descripcion: String,
    val objetosVisibles: List<String> = emptyList(),
    val jugadoresPresentes: List<String> = emptyList(),
    val salidas: List<String> = emptyList()
)

/**
 * Catálogo inmutable de entidades canónicas del mundo: habitaciones, objetos conocidos y NPCs.
 * Sirve como fuente de verdad para impedir que los jugadores inventen elementos inexistentes.
 */
data class CatalogoMundo(
    val habitaciones: List<Habitacion>,
    val objetosPorHabitacion: Map<String, List<String>>,
    val npcsConocidos: List<String>,
    val objetosConocidosGlobales: List<String>,
    val habitacionesEjercicio: List<String> = listOf("Gimnasio", "Teatro")
)

/**
 * Resultado devuelto por [ValidadorAcciones] tras evaluar una acción propuesta.
 */
data class ResultadoValidacion(
    val exitoso: Boolean,
    val razon: String,
    val efectosMecanicos: Map<String, Int> = emptyMap(),
    val tipoDetectado: TipoAccion = TipoAccion.ESPERAR,
    val habitacionDestino: String? = null,
    val comandoTraducido: String? = null
)

/**
 * Contenedores auxiliares para deserializar initial_state.json y books.json desde assets.
 */
@Serializable
data class EstadoInicialJson(
    val dia: String = "MAÑANA",
    val turnoGlobal: Int = 1,
    val jugadores: List<JugadorInicialJson> = emptyList(),
    val objetosVisibles: Map<String, List<String>> = emptyMap()
)

@Serializable
data class JugadorInicialJson(
    val id: String,
    val nombre: String,
    val habitacion: String,
    val hp: Int = 20,
    val hpMax: Int = 20,
    val estado: EstadoJugador = EstadoJugador.ACTIVO,
    val inventario: List<String> = emptyList(),
    val exp: Map<String, Int> = emptyMap()
)

@Serializable
data class BibliotecaJson(
    val libros: List<Libro>
)

/**
 * Estado global completo de la partida persistido en DataStore Preferences.
 */
@Serializable
data class EstadoPartidaPersistido(
    val turno: Turno,
    val jugadores: List<Jugador>,
    val objetosPorHabitacion: Map<String, List<String>>,
    val lecturasPorLibro: Map<String, Int> = emptyMap(),
    val historialAcciones: List<Accion> = emptyList()
)
