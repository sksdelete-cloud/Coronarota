package com.ecosdelacoronarota.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ecosdelacoronarota.ui.screens.BibliotecaScreen
import com.ecosdelacoronarota.ui.screens.EstadoScreen
import com.ecosdelacoronarota.ui.screens.GmPanelScreen
import com.ecosdelacoronarota.ui.screens.MapaScreen
import com.ecosdelacoronarota.ui.theme.AzulElectrico
import com.ecosdelacoronarota.ui.theme.FondoAbismo
import com.ecosdelacoronarota.ui.theme.RojoSangre
import com.ecosdelacoronarota.ui.theme.SuperficieOscura
import com.ecosdelacoronarota.ui.theme.TextoCeniza
import com.ecosdelacoronarota.ui.theme.TextoMarfil
import com.ecosdelacoronarota.viewmodel.GameViewModel

/**
 * Rutas de las 4 pestañas principales en Navigation Compose.
 */
sealed class DestinoNavegacion(
    val ruta: String,
    val etiqueta: String,
    val icono: ImageVector
) {
    data object Estado : DestinoNavegacion("estado", "Estado", Icons.Outlined.Groups)
    data object Mapa : DestinoNavegacion("mapa", "Mapa", Icons.Outlined.Map)
    data object Biblioteca : DestinoNavegacion("biblioteca", "Biblioteca", Icons.Outlined.AutoStories)
    data object PanelGm : DestinoNavegacion("panel_gm", "Panel GM", Icons.Outlined.EditNote)
}

/**
 * Contenedor raíz de navegación con barra inferior de 4 pestañas conectado al [GameViewModel].
 */
@Composable
fun EcosApp(viewModel: GameViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val destinos = listOf(
        DestinoNavegacion.Estado,
        DestinoNavegacion.Mapa,
        DestinoNavegacion.Biblioteca,
        DestinoNavegacion.PanelGm
    )

    Scaffold(
        containerColor = FondoAbismo,
        bottomBar = {
            NavigationBar(containerColor = SuperficieOscura) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val destinoActual = navBackStackEntry?.destination
                destinos.forEach { destino ->
                    val seleccionado = destinoActual?.hierarchy?.any { it.route == destino.ruta } == true
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = destino.icono, contentDescription = destino.etiqueta)
                        },
                        label = { Text(destino.etiqueta) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AzulElectrico,
                            selectedTextColor = TextoMarfil,
                            indicatorColor = RojoSangre.copy(alpha = 0.35f),
                            unselectedIconColor = TextoCeniza,
                            unselectedTextColor = TextoCeniza
                        )
                    )
                }
            }
        }
    ) { paddingInterno ->
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AzulElectrico)
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = DestinoNavegacion.Estado.ruta,
                modifier = Modifier.padding(paddingInterno)
            ) {
                composable(DestinoNavegacion.Estado.ruta) {
                    EstadoScreen(
                        turno = uiState.turno,
                        jugadores = uiState.jugadores,
                        onGuardarNota = viewModel::actualizarNotaJugador
                    )
                }
                composable(DestinoNavegacion.Mapa.ruta) {
                    MapaScreen(
                        turno = uiState.turno,
                        habitaciones = uiState.habitaciones
                    )
                }
                composable(DestinoNavegacion.Biblioteca.ruta) {
                    BibliotecaScreen(
                        turno = uiState.turno,
                        libros = uiState.libros,
                        lecturasPorLibro = uiState.lecturasPorLibro,
                        onRegistrarLectura = viewModel::registrarLecturaLibro
                    )
                }
                composable(DestinoNavegacion.PanelGm.ruta) {
                    GmPanelScreen(
                        turno = uiState.turno,
                        jugadores = uiState.jugadores,
                        jugadorSeleccionadoId = uiState.jugadorSeleccionadoId,
                        ultimoResultado = uiState.ultimoResultadoValidacion,
                        historialAcciones = uiState.historialAcciones,
                        modeloIaCargado = uiState.modeloIaCargado,
                        cargandoModeloIa = uiState.cargandoModeloIa,
                        nombreModeloIa = uiState.nombreModeloIa,
                        errorModeloIa = uiState.errorModeloIa,
                        procesandoAccionIa = uiState.procesandoAccionIa,
                        ultimoComandoTraducido = uiState.ultimoComandoTraducido,
                        narracionActual = uiState.narracionActual,
                        onSeleccionarJugador = viewModel::seleccionarJugadorActivoEnPanel,
                        onCargarModeloIa = viewModel::cargarModeloIa,
                        onLimpiarErrorModeloIa = viewModel::limpiarErrorModeloIa,
                        onValidarYEnviar = viewModel::validarYEnviarAccion,
                        onAvanzarTurno = viewModel::avanzarTurno
                    )
                }
            }
        }
    }
}
