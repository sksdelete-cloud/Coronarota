package com.ecosdelacoronarota.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.OutlinedTextField
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
import com.ecosdelacoronarota.data.Jugador
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.ui.components.BannerTurnoSuperior
import com.ecosdelacoronarota.ui.components.BarraHpVisual
import com.ecosdelacoronarota.ui.components.SeparadorOscuro
import com.ecosdelacoronarota.ui.components.TextoMetadatos
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.BordeSutil
import com.ecosdelacoronarota.ui.theme.SuperficieElevada
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil

/**
 * PANTALLA 1: ESTADO
 * Muestra los 12 jugadores con barra visual de HP, estado, habitación e inventario.
 * Al tocar un jugador, abre un diálogo de detalle con su EXP por conocimiento, objetos y notas.
 */
@Composable
fun EstadoScreen(
    turno: Turno,
    jugadores: List<Jugador>,
    onGuardarNota: (jugadorId: String, nuevaNota: String) -> Unit
) {
    var jugadorEnDetalle by remember { mutableStateOf<Jugador?>(null) }

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
                text = "Estado de los 12 Jugadores",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            TextoMetadatos(
                partes = listOf(
                    "${jugadores.count { it.estado.name == "ACTIVO" }} activos",
                    "${jugadores.count { it.estado.name == "INCONSCIENTE" }} inconscientes",
                    "${jugadores.count { it.estado.name == "MUERTO" }} muertos"
                )
            )
        }

        items(jugadores, key = { it.id }) { jugador ->
            TarjetaJugador(
                jugador = jugador,
                onClick = { jugadorEnDetalle = jugador }
            )
        }
    }

    jugadorEnDetalle?.let { seleccionado ->
        val actualizado = jugadores.firstOrNull { it.id == seleccionado.id } ?: seleccionado
        DialogoDetalleJugador(
            jugador = actualizado,
            onDismiss = { jugadorEnDetalle = null },
            onGuardarNota = { nota ->
                onGuardarNota(actualizado.id, nota)
            }
        )
    }
}

@Composable
private fun TarjetaJugador(
    jugador: Jugador,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = jugador.nombre,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (jugador.ubicacionVisible) jugador.habitacion else "Ubicación desconocida",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoCeniza
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            BarraHpVisual(
                hpActual = jugador.hpActual,
                hpMaximo = jugador.hpMaximo,
                estado = jugador.estado
            )

            Spacer(modifier = Modifier.height(10.dp))
            SeparadorOscuro()
            Spacer(modifier = Modifier.height(8.dp))

            val utiles = jugador.contarObjetosUtiles()
            val resumenInventario = if (jugador.inventario.isEmpty()) {
                "Sin objetos"
            } else {
                jugador.inventario.joinToString(" · ")
            }
            Text(
                text = "Inventario ($utiles/3 útiles): $resumenInventario",
                style = MaterialTheme.typography.bodyMedium,
                color = TextoMarfil
            )
        }
    }
}

@Composable
private fun DialogoDetalleJugador(
    jugador: Jugador,
    onDismiss: () -> Unit,
    onGuardarNota: (String) -> Unit
) {
    var textoNota by remember(jugador.id) { mutableStateOf(jugador.notas) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SuperficieElevada,
        title = {
            Column {
                Text(text = jugador.nombre, style = MaterialTheme.typography.headlineMedium)
                TextoMetadatos(partes = listOf(jugador.estado.name, jugador.habitacion))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BarraHpVisual(
                    hpActual = jugador.hpActual,
                    hpMaximo = jugador.hpMaximo,
                    estado = jugador.estado
                )

                Text(
                    text = "Objetos (${jugador.contarObjetosUtiles()}/3 útiles — Llave y Diario no cuentan)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (jugador.inventario.isEmpty()) "Vacío" else jugador.inventario.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoMarfil
                )

                SeparadorOscuro()

                Text(
                    text = "EXP por conocimiento",
                    style = MaterialTheme.typography.titleMedium
                )
                if (jugador.exp.isEmpty()) {
                    Text(
                        text = "Sin EXP registrada todavía.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoCeniza
                    )
                } else {
                    jugador.exp.forEach { (tomo, valor) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = tomo, style = MaterialTheme.typography.bodyMedium, color = TextoMarfil)
                            Text(text = "$valor/100", style = MaterialTheme.typography.labelMedium, color = AzulElectrico)
                        }
                    }
                }

                SeparadorOscuro()

                OutlinedTextField(
                    value = textoNota,
                    onValueChange = { textoNota = it },
                    label = { Text("Notas del GM") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGuardarNota(textoNota)
                onDismiss()
            }) {
                Text("Guardar nota", color = AzulElectrico)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = TextoCeniza)
            }
        }
    )
}
