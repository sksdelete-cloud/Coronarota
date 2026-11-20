package com.ecosdelacoronarota.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ecosdelacoronarota.data.EstadoJugador
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.ui.theme.AmbarAlerta
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.BordeSutil
import com.ecosdelacoronarota.ui.theme.RojoSangre
import com.ecosdelacoronarota.ui.theme.RojoSangreBrillante
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil
import com.ecosdelacoronarota.ui.theme.VerdeVital

/**
 * Indicador superior persistente que muestra Día, momento, turno global (ej. "Día 1 — Tarde — Turno 7/15")
 * y las acciones restantes del turno actual.
 */
@Composable
fun BannerTurnoSuperior(turno: Turno, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SuperficieOscura,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BordeSutil)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = turno.textoIndicador(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextoMarfil
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Acciones restantes: ${turno.accionesRestantes}",
                    style = MaterialTheme.typography.labelMedium,
                    color = AzulElectrico
                )
            }
        }
    }
}

/**
 * Barra visual de vitalidad (HP) que cambia de color según la proporción restante.
 */
@Composable
fun BarraHpVisual(
    hpActual: Int,
    hpMaximo: Int,
    estado: EstadoJugador,
    modifier: Modifier = Modifier
) {
    val proporcion = if (hpMaximo > 0) hpActual.toFloat() / hpMaximo.toFloat() else 0f
    val colorBarra = when {
        estado == EstadoJugador.MUERTO || hpActual <= 0 -> RojoSangre
        proporcion <= 0.3f -> RojoSangreBrillante
        proporcion <= 0.55f -> AmbarAlerta
        else -> VerdeVital
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "HP $hpActual/$hpMaximo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextoMarfil
            )
            Text(
                text = estado.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when (estado) {
                    EstadoJugador.ACTIVO -> AzulElectrico
                    EstadoJugador.INCONSCIENTE -> AmbarAlerta
                    EstadoJugador.MUERTO -> RojoSangreBrillante
                }
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { proporcion.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = colorBarra,
            trackColor = Color(0xFF26262C)
        )
    }
}

/**
 * Separador sutil entre bloques de contenido dentro de una tarjeta o hoja modal.
 */
@Composable
fun SeparadorOscuro(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BordeSutil)
    )
}

/**
 * Texto auxiliar para metadatos unificados con separador tipográfico.
 */
@Composable
fun TextoMetadatos(partes: List<String>, modifier: Modifier = Modifier) {
    Text(
        text = partes.filter { it.isNotBlank() }.joinToString(" · "),
        style = MaterialTheme.typography.labelMedium,
        color = TextoCeniza,
        modifier = modifier
    )
}
