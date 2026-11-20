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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ecosdelacoronarota.data.Capitulo
import com.ecosdelacoronarota.data.Libro
import com.ecosdelacoronarota.data.Turno
import com.ecosdelacoronarota.ui.components.BannerTurnoSuperior
import com.ecosdelacoronarota.ui.components.SeparadorOscuro
import com.ecosdelacoronarota.ui.components.TextoMetadatos
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.BordeSutil
import com.ecosdelacoronarota.ui.theme.RojoSangre
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil

/**
 * PANTALLA 3: BIBLIOTECA
 * Navegación en 3 niveles:
 * 1. Lista de los 6 tomos iniciales cargados desde assets/books.json.
 * 2. Lista de capítulos del tomo seleccionado.
 * 3. Texto completo de las páginas del capítulo con botón para registrar lectura (3 lecturas = 100/100 EXP).
 */
@Composable
fun BibliotecaScreen(
    turno: Turno,
    libros: List<Libro>,
    lecturasPorLibro: Map<String, Int>,
    onRegistrarLectura: (libroId: String) -> Unit
) {
    var libroActivo by remember { mutableStateOf<Libro?>(null) }
    var capituloActivo by remember { mutableStateOf<Capitulo?>(null) }

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

        if (libroActivo == null) {
            item {
                Text(
                    text = "Biblioteca de la Mansión",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                TextoMetadatos(
                    partes = listOf(
                        "${libros.size} tomos catalogados",
                        "Cada tomo se desbloquea al leerlo 3 veces"
                    )
                )
            }

            items(libros, key = { it.id }) { libro ->
                val lecturas = lecturasPorLibro[libro.id] ?: 0
                val expActual = if (lecturas >= 3) 100 else lecturas * 33
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { libroActivo = libro },
                    color = SuperficieOscura,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BordeSutil)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TextoMetadatos(partes = listOf(libro.categoria, "${libro.capitulos.size} capítulos"))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = libro.titulo,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SeparadorOscuro()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "EXP: $expActual/100",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (expActual >= 100) AzulElectrico else TextoMarfil
                            )
                            Text(
                                text = if (lecturas >= 3) "Desbloqueado (3/3 lecturas)" else "Se desbloquea al leerlo 3 veces ($lecturas/3)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextoCeniza
                            )
                        }
                    }
                }
            }
        } else if (capituloActivo == null) {
            val tomo = libroActivo!!
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { libroActivo = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver a libros",
                            tint = AzulElectrico
                        )
                    }
                    Column {
                        Text(text = tomo.titulo, style = MaterialTheme.typography.titleLarge)
                        TextoMetadatos(partes = listOf(tomo.categoria, "Selecciona un capítulo"))
                    }
                }
            }

            items(tomo.capitulos, key = { "${tomo.id}_${it.numero}" }) { capitulo ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { capituloActivo = capitulo },
                    color = SuperficieOscura,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BordeSutil)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = capitulo.titulo, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextoMetadatos(partes = listOf("${capitulo.paginas.size} páginas"))
                    }
                }
            }
        } else {
            val tomo = libroActivo!!
            val cap = capituloActivo!!
            val lecturas = lecturasPorLibro[tomo.id] ?: 0
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { capituloActivo = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Volver a capítulos",
                            tint = AzulElectrico
                        )
                    }
                    Column {
                        Text(text = cap.titulo, style = MaterialTheme.typography.titleLarge)
                        TextoMetadatos(partes = listOf(tomo.titulo))
                    }
                }
            }

            items(cap.paginas, key = { it.titulo }) { pagina ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SuperficieOscura,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BordeSutil)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = pagina.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = AzulElectrico
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = pagina.texto,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextoMarfil
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = { onRegistrarLectura(tomo.id) },
                    enabled = lecturas < 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoSangre,
                        contentColor = TextoMarfil
                    )
                ) {
                    Text(
                        text = if (lecturas >= 3) {
                            "Conocimiento completado (3/3)"
                        } else {
                            "Registrar lectura completa ($lecturas/3)"
                        }
                    )
                }
            }
        }
    }
}
