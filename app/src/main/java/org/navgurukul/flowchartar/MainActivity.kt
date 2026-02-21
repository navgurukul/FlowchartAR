package org.navgurukul.flowchartar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.navgurukul.flowchartar.ui.screens.ARViewScreen
import org.navgurukul.flowchartar.ui.screens.HomeScreen
import org.navgurukul.flowchartar.ui.screens.SplashScreen
import org.navgurukul.flowchartar.ui.theme.FlowchartARTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlowchartARTheme {
                FlowchartARApp()
            }
        }
    }
}

@Composable
fun FlowchartARApp() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    
    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onScanClick = {
                        navController.navigate("ar_scanner")
                    }
                )
            }
            
            composable("ar_scanner") {
                ARViewScreen(
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}
