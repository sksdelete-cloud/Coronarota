package com.ecosdelacoronarota.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ecosdelacoronarota.data.Accion
import com.ecosdelacoronarota.data.EstadoJugador
import com.ecosdelacoronarota.data.Jugador
import com.ecosdelacoronarota.data.ResultadoValidacion
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.ui.components.BannerTurnoSuperior
import com.ecosdelacoronarota.ui.components.SeparadorOscuro
import com.ecosdelacoronarota.ui.components.TextoMetadatos
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.BordeSutil
import com.ecosdelacoronarota.ui.theme.RojoSangre
import com.ecosdelacoronarota.ui.theme.RojoSangreBrillante
import com.ecosdelacoronarota.ui.theme.SuperficieElevada
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil
import com.ecosdelacoronarota.ui.theme.VerdeVital

/**
 * PANTALLA 4: PANEL DEL GM (Fase 2)
 * Permite cargar un modelo .gguf local mediante SAF File Picker, elegir el jugador activo,
 * traducir lenguaje natural a comandos válidos, validarlos con [ValidadorAcciones],
 * generar la narración en el cuadro "Narración pendiente" y avanzar el turno global.
 */
@Composable
fun GmPanelScreen(
    turno: Turno,
    jugadores: List<Jugador>,
    jugadorSeleccionadoId: String,
    ultimoResultado: ResultadoValidacion?,
    historialAcciones: List<Accion>,
    modeloIaCargado: Boolean,
    cargandoModeloIa: Boolean,
    nombreModeloIa: String?,
    errorModeloIa: String?,
    procesandoAccionIa: Boolean,
    ultimoComandoTraducido: String?,
    narracionActual: String,
    onSeleccionarJugador: (String) -> Unit,
    onCargarModeloIa: (Uri) -> Unit,
    onLimpiarErrorModeloIa: () -> Unit,
    onValidarYEnviar: (String) -> Unit,
    onAvanzarTurno: () -> Unit
) {
    var textoAccion by remember { mutableStateOf("") }
    val jugadorActivo = jugadores.firstOrNull { it.id == jugadorSeleccionadoId }
    val context = LocalContext.current

    // Selector moderno SAF (Storage Access Framework): no requiere permisos en Android 13+
    val selectorGguf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onCargarModeloIa(uri)
        }
    }

    // Mostrar Toast nativo cuando ocurra algún error al cargar o ejecutar el modelo .gguf
    LaunchedEffect(errorModeloIa) {
        if (!errorModeloIa.isNullOrBlank()) {
            Toast.makeText(context, errorModeloIa, Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BannerTurnoSuperior(turno = turno)
        }

        // Botón superior para cargar el modelo .gguf e indicador de estado
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuperficieOscura,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (modeloIaCargado) VerdeVital.copy(alpha = 0.65f) else BordeSutil
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val textoEstado = when {
                            cargandoModeloIa -> "Modelo IA: Cargando..."
                            modeloIaCargado -> "Modelo IA: Cargado"
                            else -> "Modelo IA: No cargado"
                        }
                        Text(
                            text = textoEstado,
                            style = MaterialTheme.typography.titleMedium,
                            color = when {
                                cargandoModeloIa -> AzulElectrico
                                modeloIaCargado -> VerdeVital
                                else -> TextoCeniza
                            }
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nombreModeloIa ?: "Modo estricto Fase 1 activo si no hay modelo .gguf",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoCeniza
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { selectorGguf.launch(arrayOf("*/*")) },
                            enabled = !cargandoModeloIa && !procesandoAccionIa,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, AzulElectrico)
                        ) {
                            if (cargandoModeloIa) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AzulElectrico
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Cargar Modelo IA (.gguf)", color = AzulElectrico)
                        }
                    }

                    if (!errorModeloIa.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF211214),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, RojoSangreBrillante)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = errorModeloIa,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RojoSangreBrillante,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onLimpiarErrorModeloIa) {
                                    Text("Cerrar", color = TextoMarfil)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Panel del Director de Juego",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            TextoMetadatos(
                partes = listOf(
                    jugadorActivo?.let { "${it.nombre} en ${it.habitacion}" } ?: "Selecciona jugador"
                )
            )
        }

        // Selector horizontal de jugador que ejecuta la acción
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                jugadores.forEach { jugador ->
                    val seleccionado = jugador.id == jugadorSeleccionadoId
                    FilterChip(
                        selected = seleccionado,
                        onClick = { onSeleccionarJugador(jugador.id) },
                        label = {
                            val sufijo = if (jugador.estado == EstadoJugador.ACTIVO) "" else " (${jugador.estado.name.lowercase()})"
                            Text("${jugador.nombre}$sufijo")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RojoSangre,
                            selectedLabelColor = TextoMarfil,
                            containerColor = SuperficieOscura,
                            labelColor = TextoCeniza
                        )
                    )
                }
            }
        }

        // Campo grande para redactar la acción + botones Validar y Avanzar Turno
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuperficieOscura,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = textoAccion,
                        onValueChange = { textoAccion = it },
                        label = { Text("Describe la acción del jugador") },
                        placeholder = {
                            Text(
                                if (modeloIaCargado) {
                                    "Ej.: Isaac lee su diario, o Isaac camina hacia el Pasillo Norte"
                                } else {
                                    "Ej.: Isaac examina flores azules, o Isaac se mueve a Pasillo Norte"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onValidarYEnviar(textoAccion)
                            },
                            enabled = !procesandoAccionIa,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoSangre,
                                contentColor = TextoMarfil
                            )
                        ) {
                            if (procesandoAccionIa) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TextoMarfil
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Procesando...")
                            } else {
                                Text("Validar y Enviar")
                            }
                        }

                        OutlinedButton(
                            onClick = onAvanzarTurno,
                            enabled = !procesandoAccionIa,
                            border = BorderStroke(1.dp, AzulElectrico)
                        ) {
                            Text("Avanzar Turno", color = AzulElectrico)
                        }
                    }
                }
            }
        }

        // Resultado de validación (Acción registrada o error descriptivo)
        ultimoResultado?.let { res ->
            item {
                val colorBorde = if (res.exitoso) VerdeVital else RojoSangreBrillante
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SuperficieElevada,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colorBorde)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (res.exitoso) "Acción registrada" else "Acción rechazada",
                            style = MaterialTheme.typography.titleMedium,
                            color = colorBorde
                        )
                        if (!ultimoComandoTraducido.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Comando IA: $ultimoComandoTraducido",
                                style = MaterialTheme.typography.labelMedium,
                                color = AzulElectrico
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = res.razon,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextoMarfil
                        )
                    }
                }
            }
        }

        // Área de "Narración pendiente" alimentada por el Narrador de llama.cpp en Fase 2
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SuperficieOscura,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BordeSutil)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Narración pendiente",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulElectrico
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = narracionActual,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (narracionActual == "La IA narrará aquí") TextoCeniza else TextoMarfil
                    )
                }
            }
        }

        if (historialAcciones.isNotEmpty()) {
            item {
                SeparadorOscuro()
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bitácora de acciones registradas",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(historialAcciones.take(10), key = { it.id }) { acc ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF121215),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BordeSutil)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        TextoMetadatos(
                            partes = listOf(
                                "Turno ${acc.turnoGlobal}",
                                acc.jugadorNombre,
                                acc.tipo.etiqueta,
                                acc.habitacionOrigen
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = acc.descripcion, style = MaterialTheme.typography.bodyMedium, color = TextoMarfil)
                    }
                }
            }
        }
    }
}
