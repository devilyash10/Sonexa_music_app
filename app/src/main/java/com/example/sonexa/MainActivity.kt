package com.example.sonexa
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.sonexa.ui.theme.SonexaTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.sonexa.core.ui.MainScreen
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sonexa.feature.settings.SettingsViewModel


class MainActivity : ComponentActivity() {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isAmoled by settingsViewModel.isAmoledGoldEnabled.collectAsState()
            SonexaTheme(isAmoled = isAmoled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Just call MainScreen!
                    MainScreen(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SonexaTheme {
//        Greeting("Android")
//    }
//}