package com.ecosdelacoronarota.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecosdelacoronarota.data.Habitacion
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.ui.components.BannerTurnoSuperior
import com.ecosdelacoronarota.ui.components.SeparadorOscuro
import com.ecosdelacoronarota.ui.components.TextoMetadatos
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.BordeSutil
import com.ecosdelacoronarota.ui.theme.SuperficieElevada
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil

/**
 * PANTALLA 2: MAPA
 * Lista todas las habitaciones agrupadas por zona con descripción breve, jugadores presentes
 * y objetos visibles. Al tocar una habitación, muestra el detalle con sus salidas disponibles.
 */
@Composable
fun MapaScreen(
    turno: Turno,
    habitaciones: List<Habitacion>
) {
    var habitacionSeleccionada by remember { mutableStateOf<Habitacion?>(null) }
    val agrupadasPorZona = remember(habitaciones) { habitaciones.groupBy { it.zona } }

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

        item {
            Text(
                text = "Mapa de la Mansión",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            TextoMetadatos(partes = listOf("${habitaciones.size} estancias", "Toca una sala para ver salidas"))
        }

        agrupadasPorZona.forEach { (zona, salas) ->
            item(key = "zona_$zona") {
                Text(
                    text = zona,
                    style = MaterialTheme.typography.titleLarge,
                    color = AzulElectrico,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            items(salas, key = { it.id }) { habitacion ->
                TarjetaHabitacion(
                    habitacion = habitacion,
                    onClick = { habitacionSeleccionada = habitacion }
                )
            }
        }
    }

    habitacionSeleccionada?.let { sala ->
        AlertDialog(
            onDismissRequest = { habitacionSeleccionada = null },
            containerColor = SuperficieElevada,
            title = {
                Column {
                    Text(text = sala.nombre, style = MaterialTheme.typography.headlineMedium)
                    TextoMetadatos(partes = listOf("Zona: ${sala.zona}"))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = sala.descripcion,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    SeparadorOscuro()
                    Text(
                        text = "Salidas disponibles",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulElectrico
                    )
                    Text(
                        text = sala.salidas.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMarfil
                    )
                    SeparadorOscuro()
                    Text(
                        text = "Jugadores presentes: ${
                            if (sala.jugadoresPresentes.isEmpty()) "Ninguno"
                            else sala.jugadoresPresentes.joinToString(", ")
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoMarfil
                    )
                    Text(
                        text = "Objetos visibles: ${
                            if (sala.objetosVisibles.isEmpty()) "Ninguno"
                            else sala.objetosVisibles.joinToString(", ")
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoCeniza
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { habitacionSeleccionada = null }) {
                    Text("Cerrar", color = AzulElectrico)
                }
            }
        )
    }
}

@Composable
private fun TarjetaHabitacion(
    habitacion: Habitacion,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SuperficieOscura,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BordeSutil)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = habitacion.nombre,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = habitacion.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Presentes: ${
                    if (habitacion.jugadoresPresentes.isEmpty()) "Nadie"
                    else habitacion.jugadoresPresentes.joinToString(", ")
                }",
                style = MaterialTheme.typography.labelMedium,
                color = if (habitacion.jugadoresPresentes.isEmpty()) TextoCeniza else AzulElectrico
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Objetos: ${
                    if (habitacion.objetosVisibles.isEmpty()) "Sin objetos visibles"
                    else habitacion.objetosVisibles.joinToString(", ")
                }",
                style = MaterialTheme.typography.labelMedium,
                color = TextoMarfil
            )
        }
    }
}
