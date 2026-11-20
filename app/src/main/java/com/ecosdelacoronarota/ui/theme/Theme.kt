package com.ecosdelacoronarota.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Paleta de horror cósmico: negro profundo #0D0D0D, rojo sangre #8B0000 y azul eléctrico #00BFFF
val FondoAbismo = Color(0xFF0D0D0D)
val SuperficieOscura = Color(0xFF151518)
val SuperficieElevada = Color(0xFF1D1D22)
val BordeSutil = Color(0xFF2B2B33)
val RojoSangre = Color(0xFF8B0000)
val RojoSangreBrillante = Color(0xFFCF3A3A)
val AzulElectrico = Color(0xFF00BFFF)
val TextoMarfil = Color(0xFFF2EFE9)
val TextoCeniza = Color(0xFFA0A0AB)
val VerdeVital = Color(0xFF3FA36C)
val AmbarAlerta = Color(0xFFD99B26)

private val EsquemaColorOscuro = darkColorScheme(
    primary = AzulElectrico,
    onPrimary = Color(0xFF00141A),
    secondary = RojoSangre,
    onSecondary = TextoMarfil,
    background = FondoAbismo,
    onBackground = TextoMarfil,
    surface = SuperficieOscura,
    onSurface = TextoMarfil,
    surfaceVariant = SuperficieElevada,
    onSurfaceVariant = TextoCeniza,
    outline = BordeSutil,
    error = RojoSangreBrillante,
    onError = TextoMarfil
)

// Tipografía con fuentes Serif para títulos de tomos antiguos y SansSerif para lectura fluida
private val TipografiaEcos = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        color = TextoMarfil
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = TextoMarfil
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = TextoMarfil
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = TextoMarfil
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextoCeniza
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = TextoCeniza
    )
)

/**
 * Tema Material 3 oscuro por defecto para toda la aplicación.
 */
@Composable
fun EcosTema(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaColorOscuro,
        typography = TipografiaEcos,
        content = content
    )
}
