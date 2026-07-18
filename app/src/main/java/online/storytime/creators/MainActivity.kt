package online.storytime.creators

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.StoryTimeTheme
import online.storytime.creators.ui.RootView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            StoryTimeTheme {
                Surface(modifier = Modifier.fillMaxSize().background(STColor.background), color = STColor.background) {
                    RootView()
                }
            }
        }
    }
}
