package com.astro.space.apps.kshnzk

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.astro.space.apps.kshnzk.game.GameLevel
import com.astro.space.apps.kshnzk.game.MainMenuScreen
import com.astro.space.apps.kshnzk.game.SpaceGameScreen
import com.astro.space.apps.kshnzk.ui.theme.AstroSpaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        setContent {
            AstroSpaceTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val serverManager = remember { ServerManager(context) }
    
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var selectedLevel by remember { mutableStateOf<GameLevel?>(null) }
    var contentLink by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        if (serverManager.hasStoredToken()) {
            contentLink = serverManager.getStoredContentLink()
            if (contentLink != null) {
                currentScreen = Screen.Browser
            } else {
                currentScreen = Screen.MainMenu
            }
            isLoading = false
        } else {
            val (token, link) = serverManager.fetchServerData()
            if (token != null && link != null) {
                contentLink = link
                currentScreen = Screen.Browser
            } else {
                currentScreen = Screen.MainMenu
            }
            isLoading = false
        }
    }
    
    if (isLoading) {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.ui.graphics.Color.Black
        ) {
        }
    } else {
        when (currentScreen) {
            Screen.Browser -> {
                contentLink?.let { link ->
                    BrowserScreen(contentLink = link)
                }
            }
            Screen.MainMenu -> {
                MainMenuScreen(
                    onLevelSelected = { level ->
                        selectedLevel = level
                        currentScreen = Screen.Game
                    }
                )
            }
            Screen.Game -> {
                selectedLevel?.let { level ->
                    SpaceGameScreen(
                        level = level,
                        onPause = {
                        },
                        onGameOver = {
                        },
                        onBackToMenu = {
                            currentScreen = Screen.MainMenu
                            selectedLevel = null
                        }
                    )
                }
            }
            null -> {
            }
        }
    }
}

enum class Screen {
    MainMenu,
    Game,
    Browser
}