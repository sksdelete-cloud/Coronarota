package com.ecosdelacoronarota.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Instancia única de DataStore Preferences para almacenar la partida en el dispositivo
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ecos_corona_rota_prefs")

/**
 * Repositorio local encargado de:
 * 1. Cargar los libros de la biblioteca desde assets/books.json.
 * 2. Cargar el estado inicial desde assets/initial_state.json en el primer arranque.
 * 3. Persistir y recuperar el estado completo de la partida usando DataStore Preferences.
 */
class GameRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    companion object {
        private val KEY_ESTADO_PARTIDA = stringPreferencesKey("estado_partida_json")
        private val KEY_RUTA_MODELO_IA = stringPreferencesKey("ruta_modelo_ia")
    }

    /**
     * Flujo reactivo que emite la ruta local guardada del archivo .gguf o null si no hay ninguno.
     */
    val rutaModeloIaFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_RUTA_MODELO_IA]
    }

    /**
     * Flujo reactivo que emite el estado persistido o null si aún no se ha guardado partida.
     */
    val estadoGuardadoFlow: Flow<EstadoPartidaPersistido?> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_ESTADO_PARTIDA] ?: return@map null
        runCatching { json.decodeFromString<EstadoPartidaPersistido>(raw) }.getOrNull()
    }

    /**
     * Lee y parsea los 6 tomos iniciales desde assets/books.json.
     */
    fun cargarLibrosDesdeAssets(): List<Libro> {
        val raw = context.assets.open("books.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<BibliotecaJson>(raw).libros
    }

    /**
     * Lee el estado inicial (Día 1, Mañana, Turno 1/15, 12 jugadores) desde assets/initial_state.json.
     */
    fun cargarEstadoInicialDesdeAssets(): EstadoPartidaPersistido {
        val raw = context.assets.open("initial_state.json").bufferedReader().use { it.readText() }
        val inicial = json.decodeFromString<EstadoInicialJson>(raw)
        val momentoNormalizado = inicial.dia.trim().uppercase()
            .replace("Ñ", "N")
        val momento = runCatching { MomentoDia.valueOf(momentoNormalizado) }
            .getOrDefault(MomentoDia.MANANA)

        val jugadoresMapeados = inicial.jugadores.map { j ->
            Jugador(
                id = j.id,
                nombre = j.nombre,
                habitacion = j.habitacion,
                hpActual = j.hp,
                hpMaximo = j.hpMax,
                estado = j.estado,
                inventario = j.inventario,
                exp = j.exp,
                ubicacionVisible = j.estado != EstadoJugador.MUERTO,
                notas = ""
            )
        }

        return EstadoPartidaPersistido(
            turno = Turno(
                dia = 1,
                momento = momento,
                turnoGlobal = inicial.turnoGlobal,
                turnoMaximo = 15,
                accionesRestantes = 3
            ),
            jugadores = jugadoresMapeados,
            objetosPorHabitacion = inicial.objetosVisibles,
            lecturasPorLibro = emptyMap(),
            historialAcciones = emptyList()
        )
    }

    /**
     * Obtiene el estado guardado en DataStore o, si es el primer arranque, carga initial_state.json
     * y lo guarda automáticamente.
     */
    suspend fun obtenerOCrearEstadoInicial(): EstadoPartidaPersistido {
        val existente = estadoGuardadoFlow.first()
        if (existente != null) return existente
        val inicial = cargarEstadoInicialDesdeAssets()
        guardarEstado(inicial)
        return inicial
    }

    /**
     * Guarda de forma atómica el estado actual de la partida en DataStore Preferences.
     */
    suspend fun guardarEstado(estado: EstadoPartidaPersistido) {
        val serializado = json.encodeToString(estado)
        context.dataStore.edit { prefs ->
            prefs[KEY_ESTADO_PARTIDA] = serializado
        }
    }

    /**
     * Guarda la ruta POSIX del modelo .gguf en DataStore Preferences para recargarlo al iniciar.
     */
    suspend fun guardarRutaModeloIa(ruta: String?) {
        context.dataStore.edit { prefs ->
            if (ruta == null) {
                prefs.remove(KEY_RUTA_MODELO_IA)
            } else {
                prefs[KEY_RUTA_MODELO_IA] = ruta
            }
        }
    }

    /**
     * Restaura la partida al punto exacto de initial_state.json.
     */
    suspend fun reiniciarPartida(): EstadoPartidaPersistido {
        val inicial = cargarEstadoInicialDesdeAssets()
        guardarEstado(inicial)
        return inicial
    }
}
