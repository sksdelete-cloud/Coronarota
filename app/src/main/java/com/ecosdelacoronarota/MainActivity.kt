package com.ecosdelacoronarota

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ecosdelacoronarota.ui.EcosApp
import com.ecosdelacoronarota.ui.theme.EcosTema

/**
 * Actividad única principal de la aplicación "Ecos de la Corona Rota".
 * Configura el tema oscuro de horror cósmico y monta el grafo de Navigation Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcosTema {
                EcosApp()
            }
        }
    }
}
