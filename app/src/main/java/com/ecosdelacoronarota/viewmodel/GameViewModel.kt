package com.ecosdelacoronarota.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ecosdelacoronarota.data.Accion
import com.ecosdelacoronarota.data.CatalogoMundo
import com.ecosdelacoronarota.data.CatalogoMundoInicial
import com.ecosdelacoronarota.data.EstadoJugador
import com.ecosdelacoronarota.data.EstadoPartidaPersistido
import com.ecosdelacoronarota.data.GameRepository
import com.ecosdelacoronarota.data.Habitacion
import com.ecosdelacoronarota.data.Jugador
import com.ecosdelacoronarota.data.Libro
import com.ecosdelacoronarota.data.LlamaEngine
import com.ecosdelacoronarota.data.MomentoDia
import com.ecosdelacoronarota.data.ResultadoValidacion
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.data.ValidadorAcciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

/**
 * Estado unificado de la interfaz reactiva expuesto mediante [StateFlow].
 */
data class GameUiState(
    val cargando: Boolean = true,
    val turno: Turno = Turno(),
    val jugadores: List<Jugador> = emptyList(),
    val habitaciones: List<Habitacion> = emptyList(),
    val libros: List<Libro> = emptyList(),
    val lecturasPorLibro: Map<String, Int> = emptyMap(),
    val historialAcciones: List<Accion> = emptyList(),
    val catalogo: CatalogoMundo = CatalogoMundoInicial.construirCatalogo(emptyMap()),
    val ultimoResultadoValidacion: ResultadoValidacion? = null,
    val jugadorSeleccionadoId: String = "isaac-1",
    // Fase 2: Estado del motor IA local (llama.cpp)
    val modeloIaCargado: Boolean = false,
    val cargandoModeloIa: Boolean = false,
    val nombreModeloIa: String? = null,
    val rutaModeloIa: String? = null,
    val errorModeloIa: String? = null,
    val procesandoAccionIa: Boolean = false,
    val ultimoComandoTraducido: String? = null,
    val narracionActual: String = "La IA narrará aquí"
)

/**
 * ViewModel principal de la aplicación "Ecos de la Corona Rota".
 * Gestiona el flujo reactivo de jugadores, mapa, biblioteca, validación del GM,
 * persistencia en DataStore e inferencia local con llama.cpp (.gguf).
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    private val validador = ValidadorAcciones()
    private val llamaEngine = LlamaEngine(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var objetosPorHabitacionActual: Map<String, List<String>> = emptyMap()

    init {
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        viewModelScope.launch {
            val libros = repository.cargarLibrosDesdeAssets()
            val estado = repository.obtenerOCrearEstadoInicial()
            aplicarEstadoEnUi(estado, libros)

            // Si había una ruta .gguf guardada previamente y el archivo sigue en disco, recargarlo
            val rutaGuardada = repository.rutaModeloIaFlow.first()
            if (!rutaGuardada.isNullOrBlank() && File(rutaGuardada).exists()) {
                _uiState.update { it.copy(cargandoModeloIa = true, rutaModeloIa = rutaGuardada) }
                llamaEngine.cargarModeloDesdeRuta(rutaGuardada)
                    .onSuccess { nombre ->
                        _uiState.update {
                            it.copy(
                                modeloIaCargado = true,
                                cargandoModeloIa = false,
                                nombreModeloIa = nombre,
                                rutaModeloIa = rutaGuardada,
                                errorModeloIa = null
                            )
                        }
                    }
                    .onFailure { err ->
                        repository.guardarRutaModeloIa(null)
                        _uiState.update {
                            it.copy(
                                modeloIaCargado = false,
                                cargandoModeloIa = false,
                                nombreModeloIa = null,
                                rutaModeloIa = null,
                                errorModeloIa = err.message ?: "No se pudo recargar el modelo .gguf guardado."
                            )
                        }
                    }
            }
        }
    }

    private fun aplicarEstadoEnUi(estado: EstadoPartidaPersistido, libros: List<Libro>? = null) {
        objetosPorHabitacionActual = estado.objetosPorHabitacion
        val catalogo = CatalogoMundoInicial.construirCatalogo(estado.objetosPorHabitacion)
        val habitacionesSincronizadas = catalogo.habitaciones.map { hab ->
            val presentes = estado.jugadores
                .filter { it.habitacion == hab.nombre && it.estado != EstadoJugador.MUERTO }
                .map { it.nombre }
            hab.copy(jugadoresPresentes = presentes)
        }
        val catalogoFinal = catalogo.copy(habitaciones = habitacionesSincronizadas)

        _uiState.update { actual ->
            actual.copy(
                cargando = false,
                turno = estado.turno,
                jugadores = estado.jugadores,
                habitaciones = habitacionesSincronizadas,
                libros = libros ?: actual.libros,
                lecturasPorLibro = estado.lecturasPorLibro,
                historialAcciones = estado.historialAcciones,
                catalogo = catalogoFinal
            )
        }
    }

    fun seleccionarJugadorActivoEnPanel(jugadorId: String) {
        _uiState.update {
            it.copy(
                jugadorSeleccionadoId = jugadorId,
                ultimoResultadoValidacion = null,
                ultimoComandoTraducido = null
            )
        }
    }

    /**
     * Carga un archivo de modelo .gguf elegido desde el File Picker de Android
     * y guarda su ruta local en DataStore Preferences.
     */
    fun cargarModeloIa(uri: Uri) {
        if (_uiState.value.cargandoModeloIa) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargandoModeloIa = true, errorModeloIa = null) }
            llamaEngine.cargarModeloDesdeUri(uri)
                .onSuccess { (rutaGuardada, nombreArchivo) ->
                    repository.guardarRutaModeloIa(rutaGuardada)
                    _uiState.update {
                        it.copy(
                            modeloIaCargado = true,
                            cargandoModeloIa = false,
                            nombreModeloIa = nombreArchivo,
                            rutaModeloIa = rutaGuardada,
                            errorModeloIa = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            cargandoModeloIa = false,
                            errorModeloIa = error.message
                                ?: "Error al cargar el archivo .gguf. Verifica el formato y la memoria RAM disponible."
                        )
                    }
                }
        }
    }

    fun limpiarErrorModeloIa() {
        _uiState.update { it.copy(errorModeloIa = null) }
    }

    /**
     * Flujo Fase 2 de "Validar y Enviar":
     * 1. Si el modelo IA está cargado, traduce el texto natural a "ACCIÓN: Objeto/Destino".
     *    Si la IA no está cargada o falla, usa el texto original (fallback Fase 1).
     * 2. Valida la acción con [ValidadorAcciones].
     * 3. Si es válida, aplica los efectos al estado, guarda en DataStore y, si la IA está cargada,
     *    genera la narración en español para el cuadro "Narración pendiente".
     */
    fun validarYEnviarAccion(textoAccion: String) {
        val estadoActual = _uiState.value
        if (estadoActual.procesandoAccionIa) return

        val jugador = estadoActual.jugadores.firstOrNull { it.id == estadoActual.jugadorSeleccionadoId }
            ?: return
        val habitacion = estadoActual.habitaciones.firstOrNull { it.nombre == jugador.habitacion }
            ?: return

        viewModelScope.launch {
            val textoOriginal = textoAccion.trim()
            if (textoOriginal.isEmpty()) {
                val resVacio = validador.validar("", jugador, habitacion, estadoActual.catalogo)
                _uiState.update {
                    it.copy(ultimoResultadoValidacion = resVacio, ultimoComandoTraducido = null)
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    procesandoAccionIa = llamaEngine.estaCargado,
                    errorModeloIa = null
                )
            }

            // Paso 1 y 2: Traducción opcional con IA local + fallback estricto Fase 1
            var comandoParaValidar = textoOriginal
            var comandoTraducidoPorIa: String? = null

            if (llamaEngine.estaCargado) {
                val resultadoTraduccion = llamaEngine.traducirAccion(
                    textoUsuario = textoOriginal,
                    jugador = jugador,
                    habitacion = habitacion
                )
                resultadoTraduccion
                    .onSuccess { traducido ->
                        if (traducido.isNotBlank()) {
                            comandoParaValidar = traducido
                            comandoTraducidoPorIa = traducido
                        }
                    }
                    .onFailure { err ->
                        // Fallback automático a validación estricta Fase 1 con el texto original
                        _uiState.update {
                            it.copy(
                                errorModeloIa = "No se pudo traducir con IA (${err.message}). Se aplicó validación directa."
                            )
                        }
                    }
            }

            // Paso 3: Validación determinista contra el catálogo
            val resultado = validador.validar(
                accion = comandoParaValidar,
                jugador = jugador,
                habitacion = habitacion,
                catalogo = estadoActual.catalogo
            ).copy(comandoTraducido = comandoTraducidoPorIa)

            if (!resultado.exitoso) {
                _uiState.update {
                    it.copy(
                        procesandoAccionIa = false,
                        ultimoResultadoValidacion = resultado,
                        ultimoComandoTraducido = comandoTraducidoPorIa
                    )
                }
                return@launch
            }

            // Aplicar cambios mecánicos al jugador
            val deltaHp = resultado.efectosMecanicos["HP"] ?: 0
            val deltaExp = resultado.efectosMecanicos["EXP"] ?: 0
            val nuevaHabitacion = resultado.habitacionDestino ?: jugador.habitacion

            val jugadoresActualizados = estadoActual.jugadores.map { j ->
                if (j.id != jugador.id) {
                    j
                } else {
                    val nuevoHp = (j.hpActual + deltaHp).coerceIn(0, j.hpMaximo)
                    val nuevoEstado = when {
                        nuevoHp == 0 -> EstadoJugador.MUERTO
                        j.estado == EstadoJugador.INCONSCIENTE && nuevoHp > 0 -> EstadoJugador.ACTIVO
                        else -> j.estado
                    }
                    val expActualizado = if (deltaExp > 0) {
                        val claveLibro = "Magia I"
                        val valorPrevio = j.exp[claveLibro] ?: 0
                        j.exp + (claveLibro to (valorPrevio + deltaExp).coerceAtMost(100))
                    } else {
                        j.exp
                    }
                    j.copy(
                        habitacion = nuevaHabitacion,
                        hpActual = nuevoHp,
                        estado = nuevoEstado,
                        exp = expActualizado
                    )
                }
            }

            val nuevaAccion = Accion(
                id = UUID.randomUUID().toString(),
                jugadorId = jugador.id,
                jugadorNombre = jugador.nombre,
                tipo = resultado.tipoDetectado,
                descripcion = textoOriginal,
                habitacionOrigen = habitacion.nombre,
                habitacionDestino = resultado.habitacionDestino,
                turnoGlobal = estadoActual.turno.turnoGlobal,
                resumenEfectos = resultado.razon
            )

            val turnoActualizado = estadoActual.turno.copy(
                accionesRestantes = (estadoActual.turno.accionesRestantes - 1).coerceAtLeast(0)
            )

            val nuevoEstadoPersistido = EstadoPartidaPersistido(
                turno = turnoActualizado,
                jugadores = jugadoresActualizados,
                objetosPorHabitacion = objetosPorHabitacionActual,
                lecturasPorLibro = estadoActual.lecturasPorLibro,
                historialAcciones = listOf(nuevaAccion) + estadoActual.historialAcciones
            )

            aplicarEstadoEnUi(nuevoEstadoPersistido)
            repository.guardarEstado(nuevoEstadoPersistido)

            // Paso 4 y 5: Si la IA está cargada y la acción fue válida, narrar la escena
            val habitacionContexto = _uiState.value.habitaciones
                .firstOrNull { it.nombre == nuevaHabitacion }
                ?: habitacion

            var narracionGenerada = _uiState.value.narracionActual
            if (llamaEngine.estaCargado) {
                _uiState.update {
                    it.copy(
                        ultimoResultadoValidacion = resultado,
                        ultimoComandoTraducido = comandoTraducidoPorIa,
                        narracionActual = "El Director de Juego invoca la escena..."
                    )
                }
                llamaEngine.narrarAccion(
                    comandoValidado = comandoTraducidoPorIa ?: comandoParaValidar,
                    habitacion = habitacionContexto
                ).onSuccess { textoNarrado ->
                    narracionGenerada = textoNarrado
                }.onFailure { err ->
                    narracionGenerada = "La IA narrará aquí"
                    _uiState.update {
                        it.copy(
                            errorModeloIa = "Acción validada, pero falló la narración IA: ${err.message}"
                        )
                    }
                }
            } else {
                narracionGenerada = "La IA narrará aquí"
            }

            _uiState.update {
                it.copy(
                    procesandoAccionIa = false,
                    ultimoResultadoValidacion = resultado,
                    ultimoComandoTraducido = comandoTraducidoPorIa,
                    narracionActual = narracionGenerada
                )
            }
        }
    }

    /**
     * Incrementa el turno global (hasta 15), avanza la franja horaria y resetea las acciones restantes.
     */
    fun avanzarTurno() {
        val estadoActual = _uiState.value
        val turno = estadoActual.turno
        val siguienteGlobal = (turno.turnoGlobal + 1).coerceAtMost(turno.turnoMaximo)
        val momentos = MomentoDia.entries
        val siguienteMomentoIdx = (momentos.indexOf(turno.momento) + 1) % momentos.size
        val nuevoDia = if (siguienteMomentoIdx == 0) turno.dia + 1 else turno.dia

        val nuevoTurno = turno.copy(
            dia = nuevoDia,
            momento = momentos[siguienteMomentoIdx],
            turnoGlobal = siguienteGlobal,
            accionesRestantes = 3
        )

        val persistido = EstadoPartidaPersistido(
            turno = nuevoTurno,
            jugadores = estadoActual.jugadores,
            objetosPorHabitacion = objetosPorHabitacionActual,
            lecturasPorLibro = estadoActual.lecturasPorLibro,
            historialAcciones = estadoActual.historialAcciones
        )
        _uiState.update {
            it.copy(
                ultimoResultadoValidacion = null,
                ultimoComandoTraducido = null
            )
        }
        aplicarEstadoEnUi(persistido)
        viewModelScope.launch { repository.guardarEstado(persistido) }
    }

    /**
     * Registra una lectura de tomo en la Biblioteca. Al completarse 3 lecturas alcanza 100/100 EXP.
     */
    fun registrarLecturaLibro(libroId: String) {
        val estadoActual = _uiState.value
        val lecturasActuales = (estadoActual.lecturasPorLibro[libroId] ?: 0) + 1
        val nuevoMapaLecturas = estadoActual.lecturasPorLibro + (libroId to lecturasActuales.coerceAtMost(3))

        val persistido = EstadoPartidaPersistido(
            turno = estadoActual.turno,
            jugadores = estadoActual.jugadores,
            objetosPorHabitacion = objetosPorHabitacionActual,
            lecturasPorLibro = nuevoMapaLecturas,
            historialAcciones = estadoActual.historialAcciones
        )
        aplicarEstadoEnUi(persistido)
        viewModelScope.launch { repository.guardarEstado(persistido) }
    }

    /**
     * Actualiza las notas del GM sobre un jugador específico y las persiste.
     */
    fun actualizarNotaJugador(jugadorId: String, nuevaNota: String) {
        val estadoActual = _uiState.value
        val jugadoresActualizados = estadoActual.jugadores.map {
            if (it.id == jugadorId) it.copy(notas = nuevaNota) else it
        }
        val persistido = EstadoPartidaPersistido(
            turno = estadoActual.turno,
            jugadores = jugadoresActualizados,
            objetosPorHabitacion = objetosPorHabitacionActual,
            lecturasPorLibro = estadoActual.lecturasPorLibro,
            historialAcciones = estadoActual.historialAcciones
        )
        aplicarEstadoEnUi(persistido)
        viewModelScope.launch { repository.guardarEstado(persistido) }
    }

    fun reiniciarPartida() {
        viewModelScope.launch {
            val inicial = repository.reiniciarPartida()
            _uiState.update {
                it.copy(
                    ultimoResultadoValidacion = null,
                    ultimoComandoTraducido = null,
                    narracionActual = "La IA narrará aquí"
                )
            }
            aplicarEstadoEnUi(inicial)
        }
    }

    override fun onCleared() {
        super.onCleared()
        llamaEngine.liberar()
    }
}
