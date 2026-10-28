package cl.duoc.vitairos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.vitairos.navigation.AppNavHost
import cl.duoc.vitairos.ui.theme.VitairosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { VitairosTheme { AppNavHost() } }
    }
}
