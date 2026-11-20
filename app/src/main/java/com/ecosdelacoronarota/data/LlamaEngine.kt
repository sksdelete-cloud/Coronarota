package com.ecosdelacoronarota.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import dev.ffmpegkit.llama.Llama
import dev.ffmpegkit.llama.LlamaConfig
import dev.ffmpegkit.llama.LlamaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

/**
 * Motor local de inferencia basado en llama.cpp (dev.ffmpegkit-maintained:llama-android).
 * Ejecuta dos tareas 100% en el dispositivo:
 * 1. TRADUCTOR: Convierte lenguaje natural en comandos "ACCIÓN: Objeto/Destino".
 * 2. NARRADOR: Genera una narración breve de horror cósmico tras una acción válida.
 */
class LlamaEngine(private val context: Context) {

    private val mutex = Mutex()
    private var modeloActivo: LlamaModel? = null
    private var rutaActual: String? = null

    val estaCargado: Boolean
        get() = modeloActivo != null

    /**
     * Valida la cabecera mágica GGUF (bytes "GGUF") y la memoria RAM disponible
     * antes de intentar abrir el modelo en memoria nativa.
     */
    private fun verificarArchivoGgufYMemoria(archivo: File): String? {
        if (!archivo.exists() || !archivo.isFile || archivo.length() < 64L) {
            return "El archivo .gguf no existe o está dañado."
        }

        val cabecera = ByteArray(4)
        val leidos = FileInputStream(archivo).use { it.read(cabecera) }
        val esGguf = leidos == 4 &&
            cabecera[0] == 0x47.toByte() &&
            cabecera[1] == 0x47.toByte() &&
            cabecera[2] == 0x55.toByte() &&
            cabecera[3] == 0x46.toByte()

        if (!esGguf) {
            return "El archivo seleccionado no tiene cabecera GGUF válida."
        }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (activityManager != null) {
            val memInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memInfo)
            // Margen mínimo aproximado para evitar cierres por falta de RAM al mapear el modelo
            val memoriaMinimaRequerida = (archivo.length() * 1.15).toLong()
            if (memInfo.lowMemory || memInfo.availMem < memoriaMinimaRequerida / 2L) {
                return "Memoria RAM insuficiente en el teléfono para cargar este modelo (.gguf)."
            }
        }
        return null
    }

    /**
     * Copia el archivo .gguf elegido en el File Picker (SAF) al almacenamiento interno de la app
     * para obtener una ruta POSIX compatible con llama.cpp y lo carga en memoria.
     */
    suspend fun cargarModeloDesdeUri(uri: Uri): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        runCatching {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            val nombreArchivo = obtenerNombreArchivo(uri) ?: "modelo_ia.gguf"
            if (!nombreArchivo.lowercase().endsWith(".gguf")) {
                error("Selecciona un archivo con extensión .gguf válida.")
            }

            val carpetaModelos = File(context.filesDir, "models").apply { mkdirs() }
            // Limpiar copias anteriores para no saturar el almacenamiento interno
            carpetaModelos.listFiles()?.forEach { anterior ->
                if (anterior.name != nombreArchivo) anterior.delete()
            }

            val destino = File(carpetaModelos, nombreArchivo)
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            } ?: error("No se pudo abrir el archivo seleccionado desde el almacenamiento.")

            cargarArchivoEnMemoria(destino).getOrThrow()
            destino.absolutePath to nombreArchivo
        }
    }

    /**
     * Recarga un archivo .gguf previamente guardado en DataStore si sigue presente en disco.
     */
    suspend fun cargarModeloDesdeRuta(ruta: String): Result<String> = withContext(Dispatchers.IO) {
        val archivo = File(ruta)
        cargarArchivoEnMemoria(archivo).map { archivo.name }
    }

    private suspend fun cargarArchivoEnMemoria(archivo: File): Result<Unit> = mutex.withLock {
        runCatching {
            val errorValidacion = verificarArchivoGgufYMemoria(archivo)
            if (errorValidacion != null) {
                error(errorValidacion)
            }

            modeloActivo?.let { previo ->
                runCatching { Llama.releaseModel(previo) }
                modeloActivo = null
            }

            val hilos = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
            val nuevoModelo = try {
                Llama.loadModel(
                    modelPath = archivo.absolutePath,
                    config = LlamaConfig(contextSize = 2048, threads = hilos)
                )
            } catch (oom: OutOfMemoryError) {
                throw IllegalStateException("Memoria RAM insuficiente para cargar el modelo .gguf.", oom)
            } catch (link: UnsatisfiedLinkError) {
                throw IllegalStateException("No se pudo inicializar la librería nativa llama.cpp.", link)
            }

            modeloActivo = nuevoModelo
            rutaActual = archivo.absolutePath
        }
    }

    /**
     * FUNCIÓN A (TRADUCTOR): Convierte la entrada en lenguaje natural en "ACCIÓN: Objeto/Destino".
     */
    suspend fun traducirAccion(
        textoUsuario: String,
        jugador: Jugador,
        habitacion: Habitacion
    ): Result<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val modelo = modeloActivo ?: error("El modelo IA no está cargado.")
                val systemPrompt =
                    "Eres un traductor de acciones de rol. Convierte la acción del usuario en un formato de comando para el sistema. Si menciona un objeto de su inventario, usa su nombre exacto. Responde SOLO con el comando en formato 'ACCIÓN: Objeto/Destino'. Ejemplo: 'LEER: Diario personal-Isaac'."

                val promptUsuario = buildString {
                    appendLine("Jugador: ${jugador.nombre}")
                    appendLine("Habitación actual: ${habitacion.nombre}")
                    appendLine("Salidas disponibles: ${habitacion.salidas.joinToString(", ")}")
                    appendLine("Jugadores presentes: ${habitacion.jugadoresPresentes.joinToString(", ").ifEmpty { "Ninguno" }}")
                    appendLine("Objetos visibles en habitación: ${habitacion.objetosVisibles.joinToString(", ").ifEmpty { "Ninguno" }}")
                    appendLine("Inventario del jugador: ${jugador.inventario.joinToString(", ").ifEmpty { "Vacío" }}")
                    append("Texto del usuario: $textoUsuario")
                }

                val respuesta = Llama.complete(
                    model = modelo,
                    prompt = promptUsuario,
                    systemPrompt = systemPrompt,
                    maxTokens = 64
                )
                val linea = respuesta.text.trim().lineSequence()
                    .map { it.trim().trim('"', '\'', '`').trimEnd('.') }
                    .firstOrNull { it.isNotEmpty() }
                    ?: error("El modelo no devolvió un comando traducido.")
                linea
            }
        }
    }

    /**
     * FUNCIÓN B (NARRADOR): Genera una narración inmersiva de máximo 2 párrafos en español.
     */
    suspend fun narrarAccion(
        comandoValidado: String,
        habitacion: Habitacion
    ): Result<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val modelo = modeloActivo ?: error("El modelo IA no está cargado.")
                val presentes = if (habitacion.jugadoresPresentes.isEmpty()) {
                    "ningún otro investigador"
                } else {
                    habitacion.jugadoresPresentes.joinToString(", ")
                }
                val contexto = "${habitacion.nombre} (${habitacion.descripcion}). Presentes: $presentes."
                val systemPrompt =
                    "Eres el GM de un juego de horror cósmico. Narra en español la siguiente acción exitosa de forma inmersiva, breve (máximo 2 párrafos) y con tono oscuro. Acción: $comandoValidado. Contexto: $contexto."

                val respuesta = Llama.complete(
                    model = modelo,
                    prompt = "Narra ahora el desenlace inmediato de la acción.",
                    systemPrompt = systemPrompt,
                    maxTokens = 220
                )
                val texto = respuesta.text.trim()
                if (texto.isEmpty()) error("El modelo devolvió una narración vacía.")
                texto
            }
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idxNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idxNombre >= 0 && cursor.moveToFirst()) {
                return cursor.getString(idxNombre)
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    fun liberar() {
        modeloActivo?.let { runCatching { Llama.releaseModel(it) } }
        modeloActivo = null
        rutaActual = null
    }
}
